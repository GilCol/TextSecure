package org.thoughtcrime.securesms.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.mms.PartAuthority;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;

public class SaveAttachmentTask extends ProgressDialogAsyncTask<SaveAttachmentTask.Attachment, Void, Integer> {
  private static final String TAG = SaveAttachmentTask.class.getSimpleName();

  private static final int SUCCESS              = 0;
  private static final int FAILURE              = 1;
  private static final int WRITE_ACCESS_FAILURE = 2;
  private static final int DOWNLOAD_ABORTED     = 3;

  private final WeakReference<Context> contextReference;
  private final WeakReference<MasterSecret> masterSecretReference;

  private NotificationCompat.Builder notBuilder;
  private NotificationCompat.Builder notBuilderAbort;
  private NotificationManager notManager;
  private boolean abort;
  private final int notificationID = 0;


  public SaveAttachmentTask(Context context, MasterSecret masterSecret) {
    super(context, R.string.ConversationFragment_saving_attachment, R.string.ConversationFragment_saving_attachment_to_sd_card);
    this.contextReference      = new WeakReference<Context>(context);
    this.masterSecretReference = new WeakReference<MasterSecret>(masterSecret);

    notBuilder = new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.icon_notification)
            .setContentTitle("Download");

    notBuilderAbort = new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.icon_notification)
            .setContentTitle("Download");

    notManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    IntentFilter intentFilter = new IntentFilter("ABORT");
    context.registerReceiver(new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
          cancelDownload(context);
      }
    }, intentFilter);

    Intent notificationIntent = new Intent("ABORT");
    PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
    notBuilderAbort.addAction(R.drawable.icon_notification, "Abort", pIntent);
  }

  private void cancelDownload(Context context) {
      abort = true;
      Util.abortCopy();
      notManager.cancel(notificationID);
  }

  @Override
  protected Integer doInBackground(SaveAttachmentTask.Attachment... attachments) {
    if (attachments == null || attachments.length != 1 || attachments[0] == null) {
      throw new AssertionError("must pass in exactly one attachment");
    }
    Attachment attachment = attachments[0];

    try {
      Context context           = contextReference.get();
      MasterSecret masterSecret = masterSecretReference.get();

      if (!Environment.getExternalStorageDirectory().canWrite()) {
        return WRITE_ACCESS_FAILURE;
      }

      if (context == null) {
        return FAILURE;
      }

      File        mediaFile   = constructOutputFile(attachment.contentType, attachment.date);
      InputStream inputStream = PartAuthority.getPartStream(context, masterSecret, attachment.uri);

      if (inputStream == null) {
        return FAILURE;
      }

      OutputStream outputStream = new FileOutputStream(mediaFile);
      Util.copy(inputStream, outputStream);

      if(abort) {
        mediaFile.delete();
        return DOWNLOAD_ABORTED;
      }

      MediaScannerConnection.scanFile(context, new String[]{mediaFile.getAbsolutePath()},
                                      new String[]{attachment.contentType}, null);

      return SUCCESS;
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      return FAILURE;
    }
  }

  @Override
  protected void onPostExecute(Integer result) {
    super.onPostExecute(result);
    Context context = contextReference.get();
    if (context == null) return;

    switch (result) {
      case FAILURE:
        Toast.makeText(context, R.string.ConversationFragment_error_while_saving_attachment_to_sd_card,
            Toast.LENGTH_LONG).show();
        updateNotification("Download failed", false);
        break;
      case SUCCESS:
        Toast.makeText(context, R.string.ConversationFragment_success_exclamation,
            Toast.LENGTH_LONG).show();
        updateNotification("Download complete", false);
        break;
      case WRITE_ACCESS_FAILURE:
        Toast.makeText(context, R.string.ConversationFragment_unable_to_write_to_sd_card_exclamation,
            Toast.LENGTH_LONG).show();
        updateNotification("Unable to write to storage", false);
        break;
      case DOWNLOAD_ABORTED:
        Toast.makeText(context, "Download aborted",
                Toast.LENGTH_LONG).show();
        updateNotification("Download aborted", false);
        break;
    }
  }

  private void updateNotification(String message, boolean downloadInProgress) {
    if(downloadInProgress) {
      notBuilderAbort.setContentText(message).setProgress(0, 0, true);
      notManager.notify(notificationID, notBuilderAbort.build());
    } else {
      notBuilder.setContentText(message).setProgress(0, 0, false);
      notManager.notify(notificationID, notBuilder.build());
    }
  }

  @Override
  protected void onPreExecute() {
    updateNotification("Downloading...", true);
    abort = false;
}

  @Override
  protected void onCancelled() {
    super.onCancelled();
  }

  private File constructOutputFile(String contentType, long timestamp) throws IOException {
    File sdCard = Environment.getExternalStorageDirectory();
    File outputDirectory;

    if (contentType.startsWith("video/")) {
      outputDirectory = new File(sdCard.getAbsoluteFile() + File.separator + Environment.DIRECTORY_MOVIES);
    } else if (contentType.startsWith("audio/")) {
      outputDirectory = new File(sdCard.getAbsolutePath() + File.separator + Environment.DIRECTORY_MUSIC);
    } else if (contentType.startsWith("image/")) {
      outputDirectory = new File(sdCard.getAbsolutePath() + File.separator + Environment.DIRECTORY_PICTURES);
    } else {
      outputDirectory = new File(sdCard.getAbsolutePath() + File.separator + Environment.DIRECTORY_DOWNLOADS);
    }

    if (!outputDirectory.mkdirs()) Log.w(TAG, "mkdirs() returned false, attempting to continue");

    MimeTypeMap       mimeTypeMap   = MimeTypeMap.getSingleton();
    String            extension     = mimeTypeMap.getExtensionFromMimeType(contentType);
    SimpleDateFormat  dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
    String            base          = "textsecure-" + dateFormatter.format(timestamp);

    if (extension == null)
      extension = "attach";

    int i = 0;
    File file = new File(outputDirectory, base + "." + extension);
    while (file.exists()) {
      file = new File(outputDirectory, base + "-" + (++i) + "." + extension);
    }

    return file;
  }

  public static class Attachment {
    public Uri    uri;
    public String contentType;
    public long   date;

    public Attachment(Uri uri, String contentType, long date) {
      if (uri == null || contentType == null || date < 0) {
        throw new AssertionError("uri, content type, and date must all be specified");
      }
      this.uri         = uri;
      this.contentType = contentType;
      this.date        = date;
    }
  }

  public static void showWarningDialog(Context context, OnClickListener onAcceptListener) {
    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
    builder.setTitle(R.string.ConversationFragment_save_to_sd_card);
    builder.setIconAttribute(R.attr.dialog_alert_icon);
    builder.setCancelable(true);
    builder.setMessage(R.string.ConversationFragment_this_media_has_been_stored_in_an_encrypted_database_warning);
    builder.setPositiveButton(R.string.yes, onAcceptListener);
    builder.setNegativeButton(R.string.no, null);
    builder.show();
  }
}


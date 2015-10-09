
package org.thoughtcrime.securesms.mms;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.support.annotation.DrawableRes;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.util.ResUtil;

import java.io.IOException;

import ws.com.google.android.mms.pdu.PduPart;

public class FileSlide extends Slide {

    public FileSlide(Context context, Uri uri, long dataSize) throws IOException {
        super(context, constructPartFromUri(context, uri, "*/*", dataSize));
    }

    public FileSlide(Context context, MasterSecret masterSecret, PduPart part) {
        super(context, part);
    }

    @Override
    public boolean hasImage() {
        return false;
    }

    @Override
    public boolean hasAudio() {
        return false;
    }

    @Override
    public @DrawableRes int getPlaceholderRes(Theme theme) {
        return ResUtil.getDrawableRes(theme, R.attr.conversation_icon_attach_other_file);
    }

}

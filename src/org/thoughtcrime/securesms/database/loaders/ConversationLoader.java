package org.thoughtcrime.securesms.database.loaders;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.util.AbstractCursorLoader;

public class ConversationLoader extends AbstractCursorLoader {
  private final long threadId;
  private       long limit;

  public ConversationLoader(Context context, long threadId, long limit) {
    super(context);
    this.threadId = threadId;
    this.limit  = limit;
  }

  public boolean hasLimit() {
    Log.d("ConversationLoader", "hasLimit(), limit: " + limit);
    return limit > 0;
  }

  @Override
  public Cursor getCursor() {
    Log.d("ConversationLoader", "hasLimit(), threadId" + threadId + ", limit: " + limit);
    return DatabaseFactory.getMmsSmsDatabase(context).getConversation(threadId, limit);
  }
}

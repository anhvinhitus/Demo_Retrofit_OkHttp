// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: zpmsguser.proto at 162:1
package vn.com.vng.zalopay.data.protobuf;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

/**
 * Zalo Pay: 2.0
 */
public final class MessageConnectionInfo extends Message<MessageConnectionInfo, MessageConnectionInfo.Builder> {
  public static final ProtoAdapter<MessageConnectionInfo> ADAPTER = new ProtoAdapter_MessageConnectionInfo();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_USERID = 0L;

  public static final Long DEFAULT_EMBEDDATA = 0L;

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long userid;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long embeddata;

  public MessageConnectionInfo(Long userid, Long embeddata) {
    this(userid, embeddata, ByteString.EMPTY);
  }

  public MessageConnectionInfo(Long userid, Long embeddata, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.userid = userid;
    this.embeddata = embeddata;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.userid = userid;
    builder.embeddata = embeddata;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof MessageConnectionInfo)) return false;
    MessageConnectionInfo o = (MessageConnectionInfo) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(userid, o.userid)
        && Internal.equals(embeddata, o.embeddata);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (userid != null ? userid.hashCode() : 0);
      result = result * 37 + (embeddata != null ? embeddata.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (userid != null) builder.append(", userid=").append(userid);
    if (embeddata != null) builder.append(", embeddata=").append(embeddata);
    return builder.replace(0, 2, "MessageConnectionInfo{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<MessageConnectionInfo, Builder> {
    public Long userid;

    public Long embeddata;

    public Builder() {
    }

    public Builder userid(Long userid) {
      this.userid = userid;
      return this;
    }

    public Builder embeddata(Long embeddata) {
      this.embeddata = embeddata;
      return this;
    }

    @Override
    public MessageConnectionInfo build() {
      return new MessageConnectionInfo(userid, embeddata, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_MessageConnectionInfo extends ProtoAdapter<MessageConnectionInfo> {
    ProtoAdapter_MessageConnectionInfo() {
      super(FieldEncoding.LENGTH_DELIMITED, MessageConnectionInfo.class);
    }

    @Override
    public int encodedSize(MessageConnectionInfo value) {
      return (value.userid != null ? ProtoAdapter.UINT64.encodedSizeWithTag(1, value.userid) : 0)
          + (value.embeddata != null ? ProtoAdapter.UINT64.encodedSizeWithTag(2, value.embeddata) : 0)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, MessageConnectionInfo value) throws IOException {
      if (value.userid != null) ProtoAdapter.UINT64.encodeWithTag(writer, 1, value.userid);
      if (value.embeddata != null) ProtoAdapter.UINT64.encodeWithTag(writer, 2, value.embeddata);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public MessageConnectionInfo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.userid(ProtoAdapter.UINT64.decode(reader)); break;
          case 2: builder.embeddata(ProtoAdapter.UINT64.decode(reader)); break;
          default: {
            FieldEncoding fieldEncoding = reader.peekFieldEncoding();
            Object value = fieldEncoding.rawProtoAdapter().decode(reader);
            builder.addUnknownField(tag, fieldEncoding, value);
          }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public MessageConnectionInfo redact(MessageConnectionInfo value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}

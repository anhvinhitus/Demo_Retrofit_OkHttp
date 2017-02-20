// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: zpmsguser.proto at 129:1
package vn.com.vng.zalopay.data.ws.protobuf;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import okio.ByteString;

public final class MessageToGroup extends Message<MessageToGroup, MessageToGroup.Builder> {
  public static final ProtoAdapter<MessageToGroup> ADAPTER = new ProtoAdapter_MessageToGroup();

  private static final long serialVersionUID = 0L;

  public static final ByteString DEFAULT_DATA = ByteString.EMPTY;

  public static final String DEFAULT_SIGNATURE = "";

  public static final Long DEFAULT_EXPIRETIME = 0L;

  public static final String DEFAULT_PUSHTITLE = "";

  public static final String DEFAULT_PUSHEMBEDDATA = "";

  public static final Long DEFAULT_SYSTEMID = 0L;

  public static final Integer DEFAULT_MSGTYPE = 0;

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#BYTES",
      label = WireField.Label.REQUIRED
  )
  public final ByteString data;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING",
      label = WireField.Label.REQUIRED
  )
  public final String signature;

  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#INT64"
  )
  public final Long expiretime;

  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64",
      label = WireField.Label.REPEATED
  )
  public final List<Long> uids;

  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String pushtitle;

  @WireField(
      tag = 6,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String pushembeddata;

  @WireField(
      tag = 7,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long systemid;

  @WireField(
      tag = 8,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer msgtype;

  public MessageToGroup(ByteString data, String signature, Long expiretime, List<Long> uids, String pushtitle, String pushembeddata, Long systemid, Integer msgtype) {
    this(data, signature, expiretime, uids, pushtitle, pushembeddata, systemid, msgtype, ByteString.EMPTY);
  }

  public MessageToGroup(ByteString data, String signature, Long expiretime, List<Long> uids, String pushtitle, String pushembeddata, Long systemid, Integer msgtype, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.data = data;
    this.signature = signature;
    this.expiretime = expiretime;
    this.uids = Internal.immutableCopyOf("uids", uids);
    this.pushtitle = pushtitle;
    this.pushembeddata = pushembeddata;
    this.systemid = systemid;
    this.msgtype = msgtype;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.data = data;
    builder.signature = signature;
    builder.expiretime = expiretime;
    builder.uids = Internal.copyOf("uids", uids);
    builder.pushtitle = pushtitle;
    builder.pushembeddata = pushembeddata;
    builder.systemid = systemid;
    builder.msgtype = msgtype;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof MessageToGroup)) return false;
    MessageToGroup o = (MessageToGroup) other;
    return unknownFields().equals(o.unknownFields())
        && data.equals(o.data)
        && signature.equals(o.signature)
        && Internal.equals(expiretime, o.expiretime)
        && uids.equals(o.uids)
        && Internal.equals(pushtitle, o.pushtitle)
        && Internal.equals(pushembeddata, o.pushembeddata)
        && Internal.equals(systemid, o.systemid)
        && Internal.equals(msgtype, o.msgtype);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + data.hashCode();
      result = result * 37 + signature.hashCode();
      result = result * 37 + (expiretime != null ? expiretime.hashCode() : 0);
      result = result * 37 + uids.hashCode();
      result = result * 37 + (pushtitle != null ? pushtitle.hashCode() : 0);
      result = result * 37 + (pushembeddata != null ? pushembeddata.hashCode() : 0);
      result = result * 37 + (systemid != null ? systemid.hashCode() : 0);
      result = result * 37 + (msgtype != null ? msgtype.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", data=").append(data);
    builder.append(", signature=").append(signature);
    if (expiretime != null) builder.append(", expiretime=").append(expiretime);
    if (!uids.isEmpty()) builder.append(", uids=").append(uids);
    if (pushtitle != null) builder.append(", pushtitle=").append(pushtitle);
    if (pushembeddata != null) builder.append(", pushembeddata=").append(pushembeddata);
    if (systemid != null) builder.append(", systemid=").append(systemid);
    if (msgtype != null) builder.append(", msgtype=").append(msgtype);
    return builder.replace(0, 2, "MessageToGroup{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<MessageToGroup, Builder> {
    public ByteString data;

    public String signature;

    public Long expiretime;

    public List<Long> uids;

    public String pushtitle;

    public String pushembeddata;

    public Long systemid;

    public Integer msgtype;

    public Builder() {
      uids = Internal.newMutableList();
    }

    public Builder data(ByteString data) {
      this.data = data;
      return this;
    }

    public Builder signature(String signature) {
      this.signature = signature;
      return this;
    }

    public Builder expiretime(Long expiretime) {
      this.expiretime = expiretime;
      return this;
    }

    public Builder uids(List<Long> uids) {
      Internal.checkElementsNotNull(uids);
      this.uids = uids;
      return this;
    }

    public Builder pushtitle(String pushtitle) {
      this.pushtitle = pushtitle;
      return this;
    }

    public Builder pushembeddata(String pushembeddata) {
      this.pushembeddata = pushembeddata;
      return this;
    }

    public Builder systemid(Long systemid) {
      this.systemid = systemid;
      return this;
    }

    public Builder msgtype(Integer msgtype) {
      this.msgtype = msgtype;
      return this;
    }

    @Override
    public MessageToGroup build() {
      if (data == null
          || signature == null) {
        throw Internal.missingRequiredFields(data, "data",
            signature, "signature");
      }
      return new MessageToGroup(data, signature, expiretime, uids, pushtitle, pushembeddata, systemid, msgtype, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_MessageToGroup extends ProtoAdapter<MessageToGroup> {
    ProtoAdapter_MessageToGroup() {
      super(FieldEncoding.LENGTH_DELIMITED, MessageToGroup.class);
    }

    @Override
    public int encodedSize(MessageToGroup value) {
      return ProtoAdapter.BYTES.encodedSizeWithTag(1, value.data)
          + ProtoAdapter.STRING.encodedSizeWithTag(2, value.signature)
          + (value.expiretime != null ? ProtoAdapter.INT64.encodedSizeWithTag(3, value.expiretime) : 0)
          + ProtoAdapter.UINT64.asRepeated().encodedSizeWithTag(4, value.uids)
          + (value.pushtitle != null ? ProtoAdapter.STRING.encodedSizeWithTag(5, value.pushtitle) : 0)
          + (value.pushembeddata != null ? ProtoAdapter.STRING.encodedSizeWithTag(6, value.pushembeddata) : 0)
          + (value.systemid != null ? ProtoAdapter.UINT64.encodedSizeWithTag(7, value.systemid) : 0)
          + (value.msgtype != null ? ProtoAdapter.UINT32.encodedSizeWithTag(8, value.msgtype) : 0)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, MessageToGroup value) throws IOException {
      ProtoAdapter.BYTES.encodeWithTag(writer, 1, value.data);
      ProtoAdapter.STRING.encodeWithTag(writer, 2, value.signature);
      if (value.expiretime != null) ProtoAdapter.INT64.encodeWithTag(writer, 3, value.expiretime);
      ProtoAdapter.UINT64.asRepeated().encodeWithTag(writer, 4, value.uids);
      if (value.pushtitle != null) ProtoAdapter.STRING.encodeWithTag(writer, 5, value.pushtitle);
      if (value.pushembeddata != null) ProtoAdapter.STRING.encodeWithTag(writer, 6, value.pushembeddata);
      if (value.systemid != null) ProtoAdapter.UINT64.encodeWithTag(writer, 7, value.systemid);
      if (value.msgtype != null) ProtoAdapter.UINT32.encodeWithTag(writer, 8, value.msgtype);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public MessageToGroup decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.data(ProtoAdapter.BYTES.decode(reader)); break;
          case 2: builder.signature(ProtoAdapter.STRING.decode(reader)); break;
          case 3: builder.expiretime(ProtoAdapter.INT64.decode(reader)); break;
          case 4: builder.uids.add(ProtoAdapter.UINT64.decode(reader)); break;
          case 5: builder.pushtitle(ProtoAdapter.STRING.decode(reader)); break;
          case 6: builder.pushembeddata(ProtoAdapter.STRING.decode(reader)); break;
          case 7: builder.systemid(ProtoAdapter.UINT64.decode(reader)); break;
          case 8: builder.msgtype(ProtoAdapter.UINT32.decode(reader)); break;
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
    public MessageToGroup redact(MessageToGroup value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}

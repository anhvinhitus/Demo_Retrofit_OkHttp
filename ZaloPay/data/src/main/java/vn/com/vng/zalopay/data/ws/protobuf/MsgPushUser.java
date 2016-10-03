// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: zpmsguser.proto at 81:1
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
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class MsgPushUser extends Message<MsgPushUser, MsgPushUser.Builder> {
  public static final ProtoAdapter<MsgPushUser> ADAPTER = new ProtoAdapter_MsgPushUser();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_DEVICETOKEN = "";

  public static final Integer DEFAULT_OSTYPE = 0;

  public static final String DEFAULT_ALERT = "";

  public static final String DEFAULT_SOUND = "";

  public static final Integer DEFAULT_BADGE = 0;

  public static final String DEFAULT_ACTIONKEY = "";

  public static final String DEFAULT_PUSHEMBEDDATA = "";

  public static final String DEFAULT_SERVICEID = "";

  public static final String DEFAULT_MSGID = "";

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#STRING",
      label = WireField.Label.REQUIRED
  )
  public final String devicetoken;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#INT32",
      label = WireField.Label.REQUIRED
  )
  public final Integer ostype;

  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#STRING",
      label = WireField.Label.REQUIRED
  )
  public final String alert;

  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String sound;

  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  public final Integer badge;

  @WireField(
      tag = 6,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String actionkey;

  @WireField(
      tag = 7,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String pushembeddata;

  @WireField(
      tag = 8,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String serviceid;

  @WireField(
      tag = 9,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String msgid;

  public MsgPushUser(String devicetoken, Integer ostype, String alert, String sound, Integer badge, String actionkey, String pushembeddata, String serviceid, String msgid) {
    this(devicetoken, ostype, alert, sound, badge, actionkey, pushembeddata, serviceid, msgid, ByteString.EMPTY);
  }

  public MsgPushUser(String devicetoken, Integer ostype, String alert, String sound, Integer badge, String actionkey, String pushembeddata, String serviceid, String msgid, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.devicetoken = devicetoken;
    this.ostype = ostype;
    this.alert = alert;
    this.sound = sound;
    this.badge = badge;
    this.actionkey = actionkey;
    this.pushembeddata = pushembeddata;
    this.serviceid = serviceid;
    this.msgid = msgid;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.devicetoken = devicetoken;
    builder.ostype = ostype;
    builder.alert = alert;
    builder.sound = sound;
    builder.badge = badge;
    builder.actionkey = actionkey;
    builder.pushembeddata = pushembeddata;
    builder.serviceid = serviceid;
    builder.msgid = msgid;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof MsgPushUser)) return false;
    MsgPushUser o = (MsgPushUser) other;
    return unknownFields().equals(o.unknownFields())
        && devicetoken.equals(o.devicetoken)
        && ostype.equals(o.ostype)
        && alert.equals(o.alert)
        && Internal.equals(sound, o.sound)
        && Internal.equals(badge, o.badge)
        && Internal.equals(actionkey, o.actionkey)
        && Internal.equals(pushembeddata, o.pushembeddata)
        && Internal.equals(serviceid, o.serviceid)
        && Internal.equals(msgid, o.msgid);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + devicetoken.hashCode();
      result = result * 37 + ostype.hashCode();
      result = result * 37 + alert.hashCode();
      result = result * 37 + (sound != null ? sound.hashCode() : 0);
      result = result * 37 + (badge != null ? badge.hashCode() : 0);
      result = result * 37 + (actionkey != null ? actionkey.hashCode() : 0);
      result = result * 37 + (pushembeddata != null ? pushembeddata.hashCode() : 0);
      result = result * 37 + (serviceid != null ? serviceid.hashCode() : 0);
      result = result * 37 + (msgid != null ? msgid.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", devicetoken=").append(devicetoken);
    builder.append(", ostype=").append(ostype);
    builder.append(", alert=").append(alert);
    if (sound != null) builder.append(", sound=").append(sound);
    if (badge != null) builder.append(", badge=").append(badge);
    if (actionkey != null) builder.append(", actionkey=").append(actionkey);
    if (pushembeddata != null) builder.append(", pushembeddata=").append(pushembeddata);
    if (serviceid != null) builder.append(", serviceid=").append(serviceid);
    if (msgid != null) builder.append(", msgid=").append(msgid);
    return builder.replace(0, 2, "MsgPushUser{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<MsgPushUser, Builder> {
    public String devicetoken;

    public Integer ostype;

    public String alert;

    public String sound;

    public Integer badge;

    public String actionkey;

    public String pushembeddata;

    public String serviceid;

    public String msgid;

    public Builder() {
    }

    public Builder devicetoken(String devicetoken) {
      this.devicetoken = devicetoken;
      return this;
    }

    public Builder ostype(Integer ostype) {
      this.ostype = ostype;
      return this;
    }

    public Builder alert(String alert) {
      this.alert = alert;
      return this;
    }

    public Builder sound(String sound) {
      this.sound = sound;
      return this;
    }

    public Builder badge(Integer badge) {
      this.badge = badge;
      return this;
    }

    public Builder actionkey(String actionkey) {
      this.actionkey = actionkey;
      return this;
    }

    public Builder pushembeddata(String pushembeddata) {
      this.pushembeddata = pushembeddata;
      return this;
    }

    public Builder serviceid(String serviceid) {
      this.serviceid = serviceid;
      return this;
    }

    public Builder msgid(String msgid) {
      this.msgid = msgid;
      return this;
    }

    @Override
    public MsgPushUser build() {
      if (devicetoken == null
          || ostype == null
          || alert == null) {
        throw Internal.missingRequiredFields(devicetoken, "devicetoken",
            ostype, "ostype",
            alert, "alert");
      }
      return new MsgPushUser(devicetoken, ostype, alert, sound, badge, actionkey, pushembeddata, serviceid, msgid, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_MsgPushUser extends ProtoAdapter<MsgPushUser> {
    ProtoAdapter_MsgPushUser() {
      super(FieldEncoding.LENGTH_DELIMITED, MsgPushUser.class);
    }

    @Override
    public int encodedSize(MsgPushUser value) {
      return ProtoAdapter.STRING.encodedSizeWithTag(1, value.devicetoken)
          + ProtoAdapter.INT32.encodedSizeWithTag(2, value.ostype)
          + ProtoAdapter.STRING.encodedSizeWithTag(3, value.alert)
          + (value.sound != null ? ProtoAdapter.STRING.encodedSizeWithTag(4, value.sound) : 0)
          + (value.badge != null ? ProtoAdapter.INT32.encodedSizeWithTag(5, value.badge) : 0)
          + (value.actionkey != null ? ProtoAdapter.STRING.encodedSizeWithTag(6, value.actionkey) : 0)
          + (value.pushembeddata != null ? ProtoAdapter.STRING.encodedSizeWithTag(7, value.pushembeddata) : 0)
          + (value.serviceid != null ? ProtoAdapter.STRING.encodedSizeWithTag(8, value.serviceid) : 0)
          + (value.msgid != null ? ProtoAdapter.STRING.encodedSizeWithTag(9, value.msgid) : 0)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, MsgPushUser value) throws IOException {
      ProtoAdapter.STRING.encodeWithTag(writer, 1, value.devicetoken);
      ProtoAdapter.INT32.encodeWithTag(writer, 2, value.ostype);
      ProtoAdapter.STRING.encodeWithTag(writer, 3, value.alert);
      if (value.sound != null) ProtoAdapter.STRING.encodeWithTag(writer, 4, value.sound);
      if (value.badge != null) ProtoAdapter.INT32.encodeWithTag(writer, 5, value.badge);
      if (value.actionkey != null) ProtoAdapter.STRING.encodeWithTag(writer, 6, value.actionkey);
      if (value.pushembeddata != null) ProtoAdapter.STRING.encodeWithTag(writer, 7, value.pushembeddata);
      if (value.serviceid != null) ProtoAdapter.STRING.encodeWithTag(writer, 8, value.serviceid);
      if (value.msgid != null) ProtoAdapter.STRING.encodeWithTag(writer, 9, value.msgid);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public MsgPushUser decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.devicetoken(ProtoAdapter.STRING.decode(reader)); break;
          case 2: builder.ostype(ProtoAdapter.INT32.decode(reader)); break;
          case 3: builder.alert(ProtoAdapter.STRING.decode(reader)); break;
          case 4: builder.sound(ProtoAdapter.STRING.decode(reader)); break;
          case 5: builder.badge(ProtoAdapter.INT32.decode(reader)); break;
          case 6: builder.actionkey(ProtoAdapter.STRING.decode(reader)); break;
          case 7: builder.pushembeddata(ProtoAdapter.STRING.decode(reader)); break;
          case 8: builder.serviceid(ProtoAdapter.STRING.decode(reader)); break;
          case 9: builder.msgid(ProtoAdapter.STRING.decode(reader)); break;
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
    public MsgPushUser redact(MsgPushUser value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}

// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: zpmsguser.proto at 134:1
package vn.com.vng.zalopay.data.protobuf;

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
import okio.ByteString;

/**
 * Wrapper for all messages sent from server to client
 * Actual message type is defined by field: msgtype - MessageType
 * Actual message content is defined by field: data
 * Zalo Pay: 2.0
 */
public final class DataResponseUser extends Message<DataResponseUser, DataResponseUser.Builder> {
  public static final ProtoAdapter<DataResponseUser> ADAPTER = new ProtoAdapter_DataResponseUser();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_MSGTYPE = 0;

  public static final ByteString DEFAULT_DATA = ByteString.EMPTY;

  public static final Integer DEFAULT_STATUS = 0;

  public static final Long DEFAULT_MTAID = 0L;

  public static final Long DEFAULT_MTUID = 0L;

  public static final Integer DEFAULT_SOURCEID = 0;

  public static final Long DEFAULT_USRID = 0L;

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  public final Integer msgtype;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#BYTES"
  )
  public final ByteString data;

  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  public final Integer status;

  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long mtaid;

  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long mtuid;

  @WireField(
      tag = 6,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer sourceid;

  /**
   * Zalo Pay 2.11
   */
  @WireField(
      tag = 7,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long usrid;

  public DataResponseUser(Integer msgtype, ByteString data, Integer status, Long mtaid, Long mtuid, Integer sourceid, Long usrid) {
    this(msgtype, data, status, mtaid, mtuid, sourceid, usrid, ByteString.EMPTY);
  }

  public DataResponseUser(Integer msgtype, ByteString data, Integer status, Long mtaid, Long mtuid, Integer sourceid, Long usrid, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.msgtype = msgtype;
    this.data = data;
    this.status = status;
    this.mtaid = mtaid;
    this.mtuid = mtuid;
    this.sourceid = sourceid;
    this.usrid = usrid;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.msgtype = msgtype;
    builder.data = data;
    builder.status = status;
    builder.mtaid = mtaid;
    builder.mtuid = mtuid;
    builder.sourceid = sourceid;
    builder.usrid = usrid;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof DataResponseUser)) return false;
    DataResponseUser o = (DataResponseUser) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(msgtype, o.msgtype)
        && Internal.equals(data, o.data)
        && Internal.equals(status, o.status)
        && Internal.equals(mtaid, o.mtaid)
        && Internal.equals(mtuid, o.mtuid)
        && Internal.equals(sourceid, o.sourceid)
        && Internal.equals(usrid, o.usrid);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (msgtype != null ? msgtype.hashCode() : 0);
      result = result * 37 + (data != null ? data.hashCode() : 0);
      result = result * 37 + (status != null ? status.hashCode() : 0);
      result = result * 37 + (mtaid != null ? mtaid.hashCode() : 0);
      result = result * 37 + (mtuid != null ? mtuid.hashCode() : 0);
      result = result * 37 + (sourceid != null ? sourceid.hashCode() : 0);
      result = result * 37 + (usrid != null ? usrid.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (msgtype != null) builder.append(", msgtype=").append(msgtype);
    if (data != null) builder.append(", data=").append(data);
    if (status != null) builder.append(", status=").append(status);
    if (mtaid != null) builder.append(", mtaid=").append(mtaid);
    if (mtuid != null) builder.append(", mtuid=").append(mtuid);
    if (sourceid != null) builder.append(", sourceid=").append(sourceid);
    if (usrid != null) builder.append(", usrid=").append(usrid);
    return builder.replace(0, 2, "DataResponseUser{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<DataResponseUser, Builder> {
    public Integer msgtype;

    public ByteString data;

    public Integer status;

    public Long mtaid;

    public Long mtuid;

    public Integer sourceid;

    public Long usrid;

    public Builder() {
    }

    public Builder msgtype(Integer msgtype) {
      this.msgtype = msgtype;
      return this;
    }

    public Builder data(ByteString data) {
      this.data = data;
      return this;
    }

    public Builder status(Integer status) {
      this.status = status;
      return this;
    }

    public Builder mtaid(Long mtaid) {
      this.mtaid = mtaid;
      return this;
    }

    public Builder mtuid(Long mtuid) {
      this.mtuid = mtuid;
      return this;
    }

    public Builder sourceid(Integer sourceid) {
      this.sourceid = sourceid;
      return this;
    }

    /**
     * Zalo Pay 2.11
     */
    public Builder usrid(Long usrid) {
      this.usrid = usrid;
      return this;
    }

    @Override
    public DataResponseUser build() {
      return new DataResponseUser(msgtype, data, status, mtaid, mtuid, sourceid, usrid, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_DataResponseUser extends ProtoAdapter<DataResponseUser> {
    ProtoAdapter_DataResponseUser() {
      super(FieldEncoding.LENGTH_DELIMITED, DataResponseUser.class);
    }

    @Override
    public int encodedSize(DataResponseUser value) {
      return (value.msgtype != null ? ProtoAdapter.INT32.encodedSizeWithTag(1, value.msgtype) : 0)
          + (value.data != null ? ProtoAdapter.BYTES.encodedSizeWithTag(2, value.data) : 0)
          + (value.status != null ? ProtoAdapter.INT32.encodedSizeWithTag(3, value.status) : 0)
          + (value.mtaid != null ? ProtoAdapter.UINT64.encodedSizeWithTag(4, value.mtaid) : 0)
          + (value.mtuid != null ? ProtoAdapter.UINT64.encodedSizeWithTag(5, value.mtuid) : 0)
          + (value.sourceid != null ? ProtoAdapter.UINT32.encodedSizeWithTag(6, value.sourceid) : 0)
          + (value.usrid != null ? ProtoAdapter.UINT64.encodedSizeWithTag(7, value.usrid) : 0)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, DataResponseUser value) throws IOException {
      if (value.msgtype != null) ProtoAdapter.INT32.encodeWithTag(writer, 1, value.msgtype);
      if (value.data != null) ProtoAdapter.BYTES.encodeWithTag(writer, 2, value.data);
      if (value.status != null) ProtoAdapter.INT32.encodeWithTag(writer, 3, value.status);
      if (value.mtaid != null) ProtoAdapter.UINT64.encodeWithTag(writer, 4, value.mtaid);
      if (value.mtuid != null) ProtoAdapter.UINT64.encodeWithTag(writer, 5, value.mtuid);
      if (value.sourceid != null) ProtoAdapter.UINT32.encodeWithTag(writer, 6, value.sourceid);
      if (value.usrid != null) ProtoAdapter.UINT64.encodeWithTag(writer, 7, value.usrid);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public DataResponseUser decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.msgtype(ProtoAdapter.INT32.decode(reader)); break;
          case 2: builder.data(ProtoAdapter.BYTES.decode(reader)); break;
          case 3: builder.status(ProtoAdapter.INT32.decode(reader)); break;
          case 4: builder.mtaid(ProtoAdapter.UINT64.decode(reader)); break;
          case 5: builder.mtuid(ProtoAdapter.UINT64.decode(reader)); break;
          case 6: builder.sourceid(ProtoAdapter.UINT32.decode(reader)); break;
          case 7: builder.usrid(ProtoAdapter.UINT64.decode(reader)); break;
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
    public DataResponseUser redact(DataResponseUser value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}

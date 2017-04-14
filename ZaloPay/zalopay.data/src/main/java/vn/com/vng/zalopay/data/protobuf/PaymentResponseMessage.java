// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: zpmsguser.proto at 221:1
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
 * Response message that Payment Connector sends to client as result of previous request
 * Zalo Pay: 2.10
 */
public final class PaymentResponseMessage extends Message<PaymentResponseMessage, PaymentResponseMessage.Builder> {
  public static final ProtoAdapter<PaymentResponseMessage> ADAPTER = new ProtoAdapter_PaymentResponseMessage();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_REQUESTID = 0L;

  public static final Integer DEFAULT_RESULTCODE = 0;

  public static final String DEFAULT_RESULTDATA = "";

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long requestid;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  public final Integer resultcode;

  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String resultdata;

  public PaymentResponseMessage(Long requestid, Integer resultcode, String resultdata) {
    this(requestid, resultcode, resultdata, ByteString.EMPTY);
  }

  public PaymentResponseMessage(Long requestid, Integer resultcode, String resultdata, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.requestid = requestid;
    this.resultcode = resultcode;
    this.resultdata = resultdata;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.requestid = requestid;
    builder.resultcode = resultcode;
    builder.resultdata = resultdata;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof PaymentResponseMessage)) return false;
    PaymentResponseMessage o = (PaymentResponseMessage) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(requestid, o.requestid)
        && Internal.equals(resultcode, o.resultcode)
        && Internal.equals(resultdata, o.resultdata);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (requestid != null ? requestid.hashCode() : 0);
      result = result * 37 + (resultcode != null ? resultcode.hashCode() : 0);
      result = result * 37 + (resultdata != null ? resultdata.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (requestid != null) builder.append(", requestid=").append(requestid);
    if (resultcode != null) builder.append(", resultcode=").append(resultcode);
    if (resultdata != null) builder.append(", resultdata=").append(resultdata);
    return builder.replace(0, 2, "PaymentResponseMessage{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<PaymentResponseMessage, Builder> {
    public Long requestid;

    public Integer resultcode;

    public String resultdata;

    public Builder() {
    }

    public Builder requestid(Long requestid) {
      this.requestid = requestid;
      return this;
    }

    public Builder resultcode(Integer resultcode) {
      this.resultcode = resultcode;
      return this;
    }

    public Builder resultdata(String resultdata) {
      this.resultdata = resultdata;
      return this;
    }

    @Override
    public PaymentResponseMessage build() {
      return new PaymentResponseMessage(requestid, resultcode, resultdata, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_PaymentResponseMessage extends ProtoAdapter<PaymentResponseMessage> {
    ProtoAdapter_PaymentResponseMessage() {
      super(FieldEncoding.LENGTH_DELIMITED, PaymentResponseMessage.class);
    }

    @Override
    public int encodedSize(PaymentResponseMessage value) {
      return (value.requestid != null ? ProtoAdapter.UINT64.encodedSizeWithTag(1, value.requestid) : 0)
          + (value.resultcode != null ? ProtoAdapter.INT32.encodedSizeWithTag(2, value.resultcode) : 0)
          + (value.resultdata != null ? ProtoAdapter.STRING.encodedSizeWithTag(3, value.resultdata) : 0)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, PaymentResponseMessage value) throws IOException {
      if (value.requestid != null) ProtoAdapter.UINT64.encodeWithTag(writer, 1, value.requestid);
      if (value.resultcode != null) ProtoAdapter.INT32.encodeWithTag(writer, 2, value.resultcode);
      if (value.resultdata != null) ProtoAdapter.STRING.encodeWithTag(writer, 3, value.resultdata);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public PaymentResponseMessage decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.requestid(ProtoAdapter.UINT64.decode(reader)); break;
          case 2: builder.resultcode(ProtoAdapter.INT32.decode(reader)); break;
          case 3: builder.resultdata(ProtoAdapter.STRING.decode(reader)); break;
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
    public PaymentResponseMessage redact(PaymentResponseMessage value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}

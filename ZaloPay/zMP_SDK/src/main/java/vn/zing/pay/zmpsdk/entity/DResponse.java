package vn.zing.pay.zmpsdk.entity;

public class DResponse extends DBaseEntity<DResponse> {
	public int returnCode = 0;
	public String returnMessage = null;
	public String zmpTransID;
}

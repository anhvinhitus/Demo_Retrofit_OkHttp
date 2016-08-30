package vn.com.zalopay.game.businnesslogic.enums;

public enum EAppGameError
{
	COMPONENT_NULL	("1"),
	DATA_INVALID	("2");

	private final String name;

    private EAppGameError(String s)
    {
        name = s;
    }

    public boolean equalsName(String otherName) 
    {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() 
    {
       return this.name;
    }
}

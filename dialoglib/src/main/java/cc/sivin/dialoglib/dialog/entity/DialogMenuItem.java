package cc.sivin.dialoglib.dialog.entity;

public class DialogMenuItem
{
	public String operName;
	public int textColor;
	public int resId;

	public DialogMenuItem(String operName, int textColor, int resId)
	{
		this.textColor = textColor;
		this.operName = operName;
		this.resId = resId;
	}
}

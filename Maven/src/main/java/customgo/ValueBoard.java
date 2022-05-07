package customgo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface ValueBoard {

	/**
	 * 获得分数列表。
	 * @return
	 */
	public Map<String,Double> getValueList();
	/**
	 * 设置分数
	 * @param Name
	 * @param Value
	 */
	public void Value(String Name,double Value);

	/**
	 * 改变分数（在基础上增加）
	 * @param Name
	 * @param Delta
	 */
	public void ValueAdd(String Name,double Delta);

	/**
	 * 获得分数
	 * @param Name
	 * @return
	 */
	public double getValue(String Name);

	/**
	 * 移除分数
	 * @param Name
	 */
	public void removeValue(String Name);

	/**
	 * 获得所有变量(key list)
	 * @return
	 */
	public Set<String> ValueList();
}

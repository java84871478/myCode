package gov.szghrs.wage.operation.manager;

import gov.szghrs.wage.dto.WageViewDTO;
import gov.szghrs.wage.exception.WageException;

import java.util.Map;

/**
 * @description
 * @author zhangJiaoyong
 * @created 2008-3-13
 * @version 2.0
 */	
public interface SameSeriesDutyChangeManager
{
	/**
	 * 公务员同系列职务变动
	 * @param map(personOid:Long人员id,
	 *            newDutyLevel:String新任职务级别, leadFlag:Integer领导状态,
	 *            startDate:String起薪日期 )
	 * @return WageViewDTO
	 * @throws WageException
	 */
	public WageViewDTO execute(Map<String, Object> map)
			throws WageException;
}

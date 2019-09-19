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
	 * ����Աͬϵ��ְ��䶯
	 * @param map(personOid:Long��Աid,
	 *            newDutyLevel:String����ְ�񼶱�, leadFlag:Integer�쵼״̬,
	 *            startDate:String��н���� )
	 * @return WageViewDTO
	 * @throws WageException
	 */
	public WageViewDTO execute(Map<String, Object> map)
			throws WageException;
}

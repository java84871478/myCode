package gov.szghrs.wage.operation.manager;

import gov.szghrs.wage.dto.WageViewDTO;
import gov.szghrs.wage.exception.WageException;

import java.util.Date;

public interface YearExaminationPromoteManager {
	 /**
     * ��ȿ�����������
     * @param personOid ��Ա
     * @param examineYear �������
     * @param startDateOfWage ��н����
     * @param wageSerie ����ϵ��
     * @return WageViewDTO
     * @throws WageException
     */
	public WageViewDTO execute(Long personOid, Integer examineYear,String wageSerie, Date startDateOfWage) throws WageException;

}
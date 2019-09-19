package gov.szghrs.wage.operation.manager;

import gov.szghrs.wage.dto.WageViewDTO;
import gov.szghrs.wage.exception.WageException;

import java.util.Date;

public interface YearExaminationPromoteManager {
	 /**
     * 年度考核正常晋升
     * @param personOid 人员
     * @param examineYear 晋升年份
     * @param startDateOfWage 起薪日期
     * @param wageSerie 工资系列
     * @return WageViewDTO
     * @throws WageException
     */
	public WageViewDTO execute(Long personOid, Integer examineYear,String wageSerie, Date startDateOfWage) throws WageException;

}
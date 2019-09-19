package gov.szghrs.wage.operation.manager.impl;

import jade.core.exception.ServiceException;
import jade.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

import gov.szghrs.base.bo.Person;
import gov.szghrs.base.bo.PersonMilitaryHistory;
import gov.szghrs.base.bo.ReviewInfo;
import gov.szghrs.base.bo.WageBasicInfo;
import gov.szghrs.base.bo.WageHistory;
import gov.szghrs.base.dao.EachYearExamLevelGradeDao;
import gov.szghrs.base.dto.BaseVacationInfoDto;
import gov.szghrs.base.dto.PersonDTO;
import gov.szghrs.base.dto.PoliticStatusPartyDto;
import gov.szghrs.base.dto.PunishmentInfoDto;
import gov.szghrs.base.dto.ReviewInfoDto;
import gov.szghrs.base.dto.SkilledWorkerInfoDTO;
import gov.szghrs.base.dto.WageBasicInfoDTO;
import gov.szghrs.base.dto.WageDto;
import gov.szghrs.base.dto.WageHistoryDTO;
import gov.szghrs.wage.common.WageBaseHelper;
import gov.szghrs.wage.common.WageLogger;
import gov.szghrs.wage.constant.AppConstant;
import gov.szghrs.wage.dao.WagePromoteYearHistoryDao;
import gov.szghrs.wage.dto.EachYearExamLevelGradeDTO;
import gov.szghrs.wage.dto.WagePromoteYearHistoryDto;
import gov.szghrs.wage.dto.WageViewDTO;
import gov.szghrs.wage.exception.WageException;
import gov.szghrs.wage.operation.manager.TransferManager;
import gov.szghrs.wage.operation.manager.YearExaminationPromoteManager;
import gov.szghrs.wage.operation.service.YearExaminationPromoteService;
import gov.szghrs.wage.structure.service.WageStructureService;
import gov.szghrs.wage.util.LogHelper;
import gov.szghrs.wage.util.OperationHelper;
import gov.szghrs.wage.util.UtilHelper;

/**
 * 
 *@description
 *                年度考核正常晋升
 *@author            zhangJiaoyong
 *@created           2008-7-3
 *@version           1.0
 *
 */
public class YearExaminationPromoteManagerImpl implements YearExaminationPromoteManager {

	//机关公务员年度考核正常晋升
	private YearExaminationPromoteService yearExaminationPromoteService;
	//机关技术工人年度考核正常晋升
	private YearExaminationPromoteService organTechYearExaminationPromoteService;
	//机关普通工人年度考核正常晋升
	private YearExaminationPromoteService organCommonYearExaminationPromoteService;
	
	private TransferManager transferManager;
	private TransferManager organWorkerTransferManager;
	
	private WageStructureService organWorkerWageStructureService;
	
	private WageStructureService wageStructureService;
	
	private WagePromoteYearHistoryDao wagePromoteYearHistoryDao;
	private EachYearExamLevelGradeDao eachYearExamLevelGradeDao;
	private WageLogger wageLogger;

	private WageBaseHelper wageBaseHelper;

	private Log	logger = LogFactory.getLog(YearExaminationPromoteManagerImpl.class);
	
	public void setOrganTechYearExaminationPromoteService(
			YearExaminationPromoteService organTechYearExaminationPromoteService)
	{
		this.organTechYearExaminationPromoteService = organTechYearExaminationPromoteService;
	}

	public void setOrganCommonYearExaminationPromoteService(
			YearExaminationPromoteService organCommonYearExaminationPromoteService)
	{
		this.organCommonYearExaminationPromoteService = organCommonYearExaminationPromoteService;
	}

	public void setOrganWorkerWageStructureService(
			WageStructureService organWorkerWageStructureService)
	{
		this.organWorkerWageStructureService = organWorkerWageStructureService;
	}

	public void setYearExaminationPromoteService(YearExaminationPromoteService yearExaminationPromoteService)
	{
		this.yearExaminationPromoteService = yearExaminationPromoteService;
	}
	
	public void setWageStructureService(WageStructureService wageStructureService)
	{
		this.wageStructureService = wageStructureService;
	}
	
	public void setWageLogger(WageLogger wageLogger)
	{
		this.wageLogger = wageLogger;
	}
	
	public void setWageBaseHelper(WageBaseHelper wageBaseHelper)
	{
		this.wageBaseHelper = wageBaseHelper;
	}
  /**
     * 年度考核正常晋升
     * @param personOid 人员
     * @param examineYear 晋升年份
     * @param startDateOfWage 起薪日期
     * @param wageSerie 工资系列
     * @return WageViewDTO
     * @throws WageException
     */
	public WageViewDTO execute(Long personOid, Integer examineYear,String wageSerie, Date startDateOfWage) throws WageException
	{
		   List<WageViewDTO> whds = getWageHistorysBeforeExameDate(personOid,examineYear);
		   WageViewDTO lastHistory = whds.get(whds.size()-1);
		   if(null != lastHistory 
				   &&(AppConstant.TRANSFER_CHANGE_TYPE.equals(lastHistory.getChangeType())
				   || AppConstant.TRANSFER_CHANGE_TYPE_DIRECTOR.equals(lastHistory.getChangeType())
				   || AppConstant.TRANSFER_CHANGE_TYPE_ORGAN.equals(lastHistory.getChangeType())))
		   {
			   //重做调入
			   return executeRepeatTransfer(lastHistory);
		   }
//		   else if(null != lastHistory 
//				   && AppConstant.PROBATION_CHANGE_TYPE.equals(lastHistory.getChangeType()))
//		   {
//			   //适用期做年度考核
//			   lastHistory.setChangeType(AppConstant.TYPE_PROMOTE);
//			   lastHistory.setStartDateOfWage(startDateOfWage);
//			   lastHistory.setCalulationProInfo("适用期人员年度考核!");
//			   return lastHistory;
//		   }
		   else
		   {
			   //年度考核
			   return executeExamine(personOid, examineYear, wageSerie, startDateOfWage);
		   }
	}

	
	/**
	 * 
	 * @param personOid
	 * @param examineYear
	 * @param wageSerie
	 * @param startDateOfWage
	 * @return
	 * @throws WageException
	 */
	private WageViewDTO executeExamine(Long personOid, Integer examineYear,String wageSerie, Date startDateOfWage) throws WageException
	{
		wageLogger.cleanLog();
		  String actualChangeType = AppConstant.TYPE_PROMOTE;;
		  Long processInsId =null;
 		if(AppConstant.WAGE_SERIES_OFFICAL.equals(wageSerie))
 		{
 			return officialExaminationPromote( personOid,  examineYear,  startDateOfWage,actualChangeType,processInsId);
 		}else if(AppConstant.WAGE_SERIES_2.equals(wageSerie))
 		{
 			return organTechWorkerExaminationPromote( personOid,  examineYear,  startDateOfWage,actualChangeType,processInsId);
 		}else if(AppConstant.WAGE_SERIES_3.equals(wageSerie))
 		{
 		   return organCommonWorkerExaminationPromote( personOid,  examineYear,  startDateOfWage,actualChangeType,processInsId);	
 		}
 		
 		WageViewDTO wv = new WageViewDTO();
 		UtilHelper.transformObject(wv);
 		return wv;
	}
	/**
	 * 调入
	 * @param lastHistory
	 * @return
	 * @throws WageException
	 */
	private WageViewDTO executeRepeatTransfer(  WageViewDTO lastHistory) throws WageException
	{
		 if(AppConstant.WAGE_SERIES_OFFICAL.equals(lastHistory.getWageSeries()))
			{
				 return transferManager.execute(UtilHelper.getMap2(new Object[][]{{AppConstant.MAP_KEY_PERSON_ID,lastHistory.getPersonOid()},
		                 {AppConstant.MAP_KEY_DUTY_LEVEL,lastHistory.getTreatLevel()},
		                 {AppConstant.MAP_KEY_LEAD_FLAG,lastHistory.getLeadFlag()+""},
		                 
		             	 {"actualChangeType", AppConstant.TYPE_PROMOTE},
		                 {AppConstant.MAP_KEY_PAY_DATE,lastHistory.getStartDateOfWage()}
		                 }
				 	));
			}
		     else
			{
		    	 return organWorkerTransferManager.execute(UtilHelper.getMap2(new Object[][]{
		    			 {AppConstant.MAP_KEY_PERSON_ID,lastHistory.getPersonOid()},
		                 {AppConstant.MAP_KEY_DUTY_LEVEL,lastHistory.getTreatLevel()},
		             	 {"actualChangeType", AppConstant.TYPE_PROMOTE},
		                 {AppConstant.MAP_KEY_PAY_DATE,lastHistory.getStartDateOfWage()}
		                 }
				 	));
			}
	}
	
   /**
    * 机关公务员年度考核正常晋升
    * @param personOid
    * @param examineYear 考核年度
    * @param startDateOfWage
    * @return
    * @throws WageException
    */	
	@SuppressWarnings("unused")
	private WageViewDTO officialExaminationPromote(Long personOid, Integer examineYear, Date startDateOfWage,String actualChangeType,Long processInsId) throws WageException
    {
		
		Map map = initParamMap(personOid, examineYear, startDateOfWage,
				actualChangeType, processInsId);
 		WageViewDTO wageViewDTO;
		wageViewDTO = yearExaminationPromoteService.execute(map);
		logger.info(wageViewDTO.getCalulationProInfo());
		return doCalWageDetail(wageViewDTO); 
    }

	private WageViewDTO getPromoteCount(List<EachYearExamLevelGradeDTO> eachYearExamLevelGrades,
			 List<WagePromoteYearHistoryDto> wagePromoteYearHistorys,Integer examineYear)
	{
		WageViewDTO returnV= new WageViewDTO();
		initExchYearExamLevelGrade(eachYearExamLevelGrades, returnV);
		initPromoteYearHistory(wagePromoteYearHistorys, examineYear, returnV);
		return returnV;
	}

	private void initExchYearExamLevelGrade(
			List<EachYearExamLevelGradeDTO> eachYearExamLevelGrades,
			WageViewDTO returnV) {
		
		if(UtilHelper.isEmpty(eachYearExamLevelGrades))
		{
			returnV.setPromoteRankCount(AppConstant.UPGRADE_INIT_NUM);
			returnV.setPromoteGradeCount(AppConstant.UPGRADE_INIT_NUM);
			returnV.setPromoteAllowCount(AppConstant.UPGRADE_INIT_NUM);
			returnV.setPromotePositionGradeCount(AppConstant.UPGRADE_INIT_NUM);
			
			WageViewDTO shenzhen = new WageViewDTO();
			shenzhen.setPromoteRankCount(AppConstant.UPGRADE_INIT_NUM);
			shenzhen.setPromoteGradeCount(AppConstant.UPGRADE_INIT_NUM);
			shenzhen.setPromoteAllowCount(AppConstant.UPGRADE_INIT_NUM);
			returnV.setSzJujiWage(shenzhen);
			return;
		}
		
		for(EachYearExamLevelGradeDTO eyd :eachYearExamLevelGrades)
		{
			
			
			
			if(AppConstant.EXAMINE_PROMOTION_TYPE_LELVE.equals(eyd.getPromotionType()))
			{
				returnV.setPromoteRankCount(eyd.getPromotionCount());
			}
			if(AppConstant.EXAMINE_PROMOTION_TYPE_GRADE.equals(eyd.getPromotionType()))
			{
				 returnV.setPromoteGradeCount(eyd.getPromotionCount());
			}
			if(AppConstant.PROMOTION_LELVE_JU.equals(eyd.getPromotionType()))
			{
				WageViewDTO sz =null;
				if(returnV.getSzJujiWage()!= null)
				{
					sz=(WageViewDTO)returnV.getSzJujiWage();
				}
				else
				{
					sz=new WageViewDTO();
					
				}
				sz.setPromoteRankCount(eyd.getPromotionCount());
				returnV.setSzJujiWage(sz);
			}
			if(AppConstant.PROMOTION_GRADE_JU.equals(eyd.getPromotionType()))
			{
				WageViewDTO sz =null;
				if(returnV.getSzJujiWage()!= null)
				{
					sz=(WageViewDTO)returnV.getSzJujiWage();
				}
				else
				{
					sz=new WageViewDTO();
					
				}
				sz.setPromoteGradeCount(eyd.getPromotionCount());
				returnV.setSzJujiWage(sz);
			}
			if(AppConstant.PROMOTION_ALLOWANCE.equals(eyd.getPromotionType()))
			{
				
				returnV.setPromoteAllowCount(eyd.getPromotionCount());
				WageViewDTO sz =null;
				if(returnV.getSzJujiWage()!= null)
				{
					sz=(WageViewDTO)returnV.getSzJujiWage();
				}
				else
				{
					sz=new WageViewDTO();
					
				}
				sz.setPromoteAllowCount(eyd.getPromotionCount());
				returnV.setSzJujiWage(sz);
				
			}
			if(AppConstant.EXAMINE_PROMOTION_TYPE_POST.equals(eyd.getPromotionType()))
			{
				 returnV.setPromotePositionGradeCount(eyd.getPromotionCount());
			}
		}
	}

	private void initPromoteYearHistory(
			List<WagePromoteYearHistoryDto> wagePromoteYearHistorys,
			Integer examineYear, WageViewDTO returnV) {
		
		if(UtilHelper.isEmpty(wagePromoteYearHistorys))
		{
			
			return;
		}
		
		for(WagePromoteYearHistoryDto wpd :wagePromoteYearHistorys)
		{
			
			if(examineYear != wpd.getPromoteYear())
			{
				continue;
			}
			if(AppConstant.EXAMINE_PROMOTION_TYPE_LELVE.equals(wpd.getPromoteType()))
			{
				returnV.setPromoteRankFlag(wpd.getResetFlag());
			}
			if(AppConstant.EXAMINE_PROMOTION_TYPE_GRADE.equals(wpd.getPromoteType()))
			{
				 returnV.setPromoteGradeFlag(wpd.getResetFlag());
			}
			if(AppConstant.PROMOTION_LELVE_JU.equals(wpd.getPromoteType()))
			{
				WageViewDTO sz =null;
				if(returnV.getSzJujiWage()!= null)
				{
					sz=(WageViewDTO)returnV.getSzJujiWage();
				}
				else
				{
					sz=new WageViewDTO();
					
				}
				sz.setPromoteRankFlag(wpd.getResetFlag());
				returnV.setSzJujiWage(sz);
			}
			if(AppConstant.PROMOTION_GRADE_JU.equals(wpd.getPromoteType()))
			{
				WageViewDTO sz =null;
				if(returnV.getSzJujiWage()!= null)
				{
					sz=(WageViewDTO)returnV.getSzJujiWage();
				}
				else
				{
					sz=new WageViewDTO();
					
				}
				sz.setPromoteGradeFlag(wpd.getResetFlag());
				returnV.setSzJujiWage(sz);
			}
			if(AppConstant.PROMOTION_ALLOWANCE.equals(wpd.getPromoteType()))
			{
				returnV.setPromoteAllowFlag(wpd.getResetFlag());
				WageViewDTO sz =null;
				if(returnV.getSzJujiWage()!= null)
				{
					sz=(WageViewDTO)returnV.getSzJujiWage();
				}
				else
				{
					sz=new WageViewDTO();
					
				}
				sz.setPromoteAllowFlag(wpd.getResetFlag());
				returnV.setSzJujiWage(sz);
			}
			if(AppConstant.EXAMINE_PROMOTION_TYPE_POST.equals(wpd.getPromoteType()))
			{
				 returnV.setPromotePositionGradeFlag(wpd.getResetFlag());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map initParamMap(Long personOid, Integer examineYear,
			Date startDateOfWage, String actualChangeType, Long processInsId)
			throws WageException {
		Map map = new HashMap();
		this.writeBaseLogInfo(personOid,actualChangeType,processInsId,examineYear ,map);
		map.put(AppConstant.PARAM_EXAMINE_YEAR, examineYear); 
		map.put(AppConstant.WAGE_START_DATE_OF_WAGE, startDateOfWage);
		
		 List<EachYearExamLevelGradeDTO> eachYearExamLevelGrades =eachYearExamLevelGradeDao.findExamLevelGrades(personOid,examineYear);
		 List<WagePromoteYearHistoryDto> wagePromoteYearHistorys= wagePromoteYearHistoryDao.findPromoteYearHistory(personOid);

		 
		 map.put(AppConstant.WAGE_PROMOTE_YEAR,  getPromoteCount(eachYearExamLevelGrades, wagePromoteYearHistorys, examineYear));
		 
		try {
			List<BaseVacationInfoDto> vacationInfoList = wageBaseHelper.findVacationInfoListByPersonId(personOid,actualChangeType,processInsId);
			 map.put(AppConstant.WAGE_VACATION_INFO, vacationInfoList);
		} catch (ServiceException e) {
			logger.info("查询休假信息异常");
		}
		try {
			List<PunishmentInfoDto> punishmentList = wageBaseHelper.findPunishmentInfoListByPersonId(personOid,actualChangeType,processInsId);
			 map.put(AppConstant.WAGE_PUNISH_INFO, punishmentList);
		} catch (ServiceException e) {
			logger.info("查询惩罚信息异常");
		};
	      
		List<PersonMilitaryHistory> militaryList = wageBaseHelper.findPersonMilitaryHistoryListByPersonId(personOid,actualChangeType,processInsId); //军队任职集
		 map.put(AppConstant.PARAM_MILITARY_HISTORY, militaryList);
		
	    List<WageViewDTO> whds = getWageHistorysBeforeExameDate(personOid,examineYear);
		map.put(AppConstant.WAGE_HISTORYS, whds);
		return map;
	}

	private List<WageViewDTO> getWageHistorysBeforeExameDate(Long personOid,
			Integer examineYear) throws WageException {
		//本次年度考核晋升日期,如果考核晋升日期滞后则还原为1月1日
		Date promotionDate = UtilHelper.getDateFromStr((examineYear + 1)+ "0101");
		// 获得该人的工资历史集
		List<WageHistory> allWages = wageBaseHelper.findAllWageHistoryBeforeDate(personOid,promotionDate);
		List<WageViewDTO> whds = new ArrayList<WageViewDTO>();
		if(UtilHelper.notEmpty(allWages))
		{
			
			for(WageHistory wh :allWages)
			{
				WageViewDTO whdo = new WageViewDTO();
				BeanUtils.copyProperties(wh, whdo);
				whds.add(whdo);
			}
			
		}
		return whds;
	}
    /**
     * 机关技术工人年度考核正常晋升
     * @param personOid
     * @param examineYear
     * @param startDateOfWage
     * @return
     * @throws WageException
     */
    @SuppressWarnings({ "unused", "unchecked" })
	private WageViewDTO organTechWorkerExaminationPromote(Long personOid, Integer examineYear, Date startDateOfWage,String actualChangeType,Long processInsId) throws WageException
    {
    	WageViewDTO wageViewDTO;
    	
    	Map map = initParamMap(personOid, examineYear, startDateOfWage,
				actualChangeType, processInsId);
		wageViewDTO = organTechYearExaminationPromoteService.execute(map);
		logger.info(wageViewDTO.getCalulationProInfo());
		
	    Map detailMap = new HashMap();
		
		detailMap.put(AppConstant.MAP_KEY_WAGE_VIEW_DTO, wageViewDTO);
		
		wageViewDTO = organWorkerWageStructureService.calWageDetail(detailMap);
		return wageViewDTO; 
    	
    }
    /**
     * 机关普通工人年度考核正常晋升
    * @param personOid
     * @param examineYear
     * @param startDateOfWage
     * @return
     * @throws WageException
     */
    @SuppressWarnings({ "unused", "unchecked" })
	private WageViewDTO organCommonWorkerExaminationPromote(Long personOid, Integer examineYear, Date startDateOfWage,String actualChangeType,Long processInsId) throws WageException
    {
    	WageViewDTO wageViewDTO;
    	Map map = initParamMap(personOid, examineYear, startDateOfWage,
				actualChangeType, processInsId);
		wageViewDTO = organCommonYearExaminationPromoteService.execute(map);
		logger.info(wageViewDTO.getCalulationProInfo());
		
	    Map detailMap = new HashMap();
		detailMap.put(AppConstant.MAP_KEY_WAGE_VIEW_DTO, wageViewDTO);
		wageViewDTO = organWorkerWageStructureService.calWageDetail(detailMap);
		return wageViewDTO; 
    }
	
	
	
	@SuppressWarnings("unchecked")
	private WageViewDTO doCalWageDetail(WageViewDTO wageViewDTO)
			throws WageException {
		Map detailMap = new HashMap();
		detailMap.put(AppConstant.MAP_KEY_WAGE_VIEW_DTO, wageViewDTO);
		wageViewDTO = wageStructureService.calWageDetail(detailMap);
		return wageViewDTO;
	}
	
	@SuppressWarnings("unchecked")
	private void writeBaseLogInfo(Long personOid,String actualChangeType,
			Long processInsId,int examineYear,Map map) throws WageException
	{
		// 任职集
		List<WageDto> positions = null;
		
		try
		{
			//本次年度考核晋升日期,如果考核晋升日期滞后则还原为1月1日
			Date promotionDate = UtilHelper.getDateFromStr((examineYear + 1)+ "0101");
			List<WageDto> pos = wageBaseHelper.getPositionsByPerson(personOid,actualChangeType,processInsId);		
			positions=OperationHelper.getPositionListForWrapChange(promotionDate, pos);
			//
			List<ReviewInfo> examines = wageBaseHelper.getYearExamineList(personOid,actualChangeType,processInsId);
			
			List<WageDto> edu = wageBaseHelper.getEducationInfoList(personOid,actualChangeType,processInsId);
			List<WageDto> educations=OperationHelper.getEducationsBeforeDate(edu,promotionDate);
			
			PersonDTO person = wageBaseHelper.getPerson(personOid,actualChangeType,processInsId);
			WageBasicInfoDTO infoDto = wageBaseHelper.getWageBasicInfo(personOid,actualChangeType,processInsId);
			WageBasicInfo wageBasicInfo = null;
			if(null != infoDto)
			{
				wageBasicInfo = new WageBasicInfo();
				BeanUtils.copyProperties(infoDto, wageBasicInfo);
			}
			// 专业技术人员的取得历任技术等级历史信息	
			List<WageDto> wageTechGradeHistoryList = wageBaseHelper
					.listWageTechGradeHistory(Long.valueOf(personOid),actualChangeType,processInsId);
//			List<SkilledWorkerInfoDTO> skilledWorkerInfoDTOList = 
//				wageBaseHelper.listSkilledWorkerInfo(Long.valueOf(personOid),actualChangeType,processInsId);
//			List<WageDto> techGradeHistoryList = new ArrayList<WageDto>();
//			if(UtilHelper.notEmpty(skilledWorkerInfoDTOList))
//			{
//				for(SkilledWorkerInfoDTO skd :skilledWorkerInfoDTOList)
//				{
//					WageDto wd  = new WageDto();
//					wd.setTechnologyGrade(skd.getSkillkerLevel());
//					wd.setPositioningDate(skd.getStartDate());
//					wd.setDisposalDate(skd.getEndDate());
//					techGradeHistoryList.add(wd);
//				}
//			}
			List<ReviewInfoDto> examinesDto = new ArrayList<ReviewInfoDto>();
			if(UtilHelper.notEmpty(examines))
			{
				for(ReviewInfo ri :examines)
				{
					ReviewInfoDto rid = new ReviewInfoDto();
					BeanUtils.copyProperties(ri, rid);
					examinesDto.add(rid);
				}	
			}
			
			
			map.put(AppConstant.WAGE_EXAMINES, examinesDto);
			map.put(AppConstant.WAGE_POSITIONS, pos);
			map.put(AppConstant.WAGE_TECHGRADEHISTORY, wageTechGradeHistoryList);
			map.put(AppConstant.WAGE_EDUCATIONS, edu);
			map.put(AppConstant.WAGE_PERSON_INFO, person);
			map.put(AppConstant.WAGE_BASEIC_INFO, infoDto);
			
			wageLogger.writeBasicInfo(person, wageBasicInfo, positions,wageTechGradeHistoryList, 
					examines, educations);
			wageLogger.writeLog("\n" + LogHelper.getLine() + "\n计算过程......\n" + LogHelper.getLine());		
		}
		catch(ServiceException e)
		{
			throw new WageException("", e);
		}
	}

	public void setWagePromoteYearHistoryDao(
			WagePromoteYearHistoryDao wagePromoteYearHistoryDao) {
		this.wagePromoteYearHistoryDao = wagePromoteYearHistoryDao;
	}

	public void setLogger(Log logger) {
		this.logger = logger;
	}

	public void setEachYearExamLevelGradeDao(
			EachYearExamLevelGradeDao eachYearExamLevelGradeDao) {
		this.eachYearExamLevelGradeDao = eachYearExamLevelGradeDao;
	}

	public void setTransferManager(TransferManager transferManager) {
		this.transferManager = transferManager;
	}

	public void setOrganWorkerTransferManager(
			TransferManager organWorkerTransferManager) {
		this.organWorkerTransferManager = organWorkerTransferManager;
	}
}

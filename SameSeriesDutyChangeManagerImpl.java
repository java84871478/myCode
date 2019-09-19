package gov.szghrs.wage.operation.manager.impl;

import java.util.Map;

import gov.szghrs.wage.constant.AppConstant;
import gov.szghrs.wage.dto.WageViewDTO;
import gov.szghrs.wage.exception.WageException;
import gov.szghrs.wage.operation.manager.SameSeriesDutyChangeManager;
import gov.szghrs.wage.operation.service.SameSeriesDutyChangeService;
import gov.szghrs.wage.structure.service.WageStructureService;
import gov.szghrs.wage.util.UtilHelper;
/**
 * 
 *@description
 *
 *@author            zhangJiaoyong
 *@created           2008-3-13
 *@version           1.0
 *
 */
public class SameSeriesDutyChangeManagerImpl implements SameSeriesDutyChangeManager
{
	//机关公务员同系列职务变动
	private SameSeriesDutyChangeService sameSeriesDutyChangeService;
	//工资结构服务
	private WageStructureService wageStructureService;
	//机关普通工人同系列职务变动
	private SameSeriesDutyChangeService organCommonWorkerSameSeriesDutyChangeService;
	//机关技术工人同系列职务变动
	private SameSeriesDutyChangeService organTechWorkerSameSeriesDutyChangeService;
	//工资结构服务
	private WageStructureService organWageStructureService;
	
	public void setOrganCommonWorkerSameSeriesDutyChangeService(
			SameSeriesDutyChangeService organCommonWorkerSameSeriesDutyChangeService)
	{
		this.organCommonWorkerSameSeriesDutyChangeService = organCommonWorkerSameSeriesDutyChangeService;
	}
	public void setOrganTechWorkerSameSeriesDutyChangeService(
			SameSeriesDutyChangeService organTechWorkerSameSeriesDutyChangeService)
	{
		this.organTechWorkerSameSeriesDutyChangeService = organTechWorkerSameSeriesDutyChangeService;
	}
	public void setOrganWageStructureService(
			WageStructureService organWageStructureService)
	{
		this.organWageStructureService = organWageStructureService;
	}
	public void setSameSeriesDutyChangeService(
			SameSeriesDutyChangeService sameSeriesDutyChangeService)
	{
		this.sameSeriesDutyChangeService = sameSeriesDutyChangeService;
	}
	public void setWageStructureService(WageStructureService wageStructureService)
	{
		this.wageStructureService = wageStructureService;
	}

	/**
	 * 同系列职务变动
	 * @param map(personOid:String人员id,
	 *            newDutyLevel:String新任职务级别, leadFlag:Integer领导状态,
	 *            startDate:String起薪日期 ,wageSerie:String工资系列,processInsId:String)
	 * @return WageViewDTO
	 * @throws WageException
	 */
     public WageViewDTO execute(Map<String,Object> map) throws WageException
     {
    	 String wageSerie = (String)map.get("wageSerie");
 		if(AppConstant.WAGE_SERIES_OFFICAL.equals(wageSerie))
 		{
 			return officialDutyChange(map);
 		}else if(AppConstant.WAGE_SERIES_2.equals(wageSerie))
 		{
 			return organTechWorkerDutyChange(map);
 		}else if(AppConstant.WAGE_SERIES_3.equals(wageSerie))
 		{
 		   return organCommonWorkerDutyChange(map);	
 		}
 		return null;
     }
     
     /**
      * 
      * @param map(personOid:String人员id,
	 *            newDutyLevel:String新任职务级别, leadFlag:Integer领导状态,
	 *            startDate:String起薪日期yyyy-mm-dd )
      * @return
      * @throws WageException
      */
     @SuppressWarnings("unused")
	private WageViewDTO officialDutyChange(Map<String,Object> map) throws WageException
     {
    	 WageViewDTO wageViewDTO = sameSeriesDutyChangeService.executeOfficalSameSeriesDutyChange(map);
 		
    	 wageViewDTO=wageStructureService.calWageDetail(UtilHelper.getMap2(new Object[][]{{"wageViewDTO",wageViewDTO},{"processInsId",map.get("processInsId")}}));

   		 return wageViewDTO;	 
     }
     /**
      * 
      * @param map(personOid:String, startDate:String起薪日期yyyy-mm-dd)
      * @return
      * @throws WageException
      */
     @SuppressWarnings("unused")
	private WageViewDTO organTechWorkerDutyChange(Map<String,Object> map) throws WageException
     {
    	 WageViewDTO wageViewDTO  = organTechWorkerSameSeriesDutyChangeService.executeOfficalSameSeriesDutyChange(map);
  		 WageViewDTO wvt =  organWageStructureService.calWageDetail(UtilHelper.getMap2(new Object[][]{{"wageViewDTO",wageViewDTO},{"processInsId",map.get("processInsId")}}));
  		
      	return wvt; 
     }
     /**
      * 
      * @param map(personOid:String, startDate:String起薪日期yyyy-mm-dd)
      * @return
      * @throws WageException
      */
     private WageViewDTO organCommonWorkerDutyChange(Map<String,Object> map) throws WageException
     {
    	 //机关普通工人无职务变动
    	 WageViewDTO wvd = new WageViewDTO();
    	 UtilHelper.transformObject(wvd);
    	 wvd.setCalulationProInfo("机关普通工人无职务变动.");
       	return  wvd; 
     }

	
}

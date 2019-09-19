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
	//���ع���Աͬϵ��ְ��䶯
	private SameSeriesDutyChangeService sameSeriesDutyChangeService;
	//���ʽṹ����
	private WageStructureService wageStructureService;
	//������ͨ����ͬϵ��ְ��䶯
	private SameSeriesDutyChangeService organCommonWorkerSameSeriesDutyChangeService;
	//���ؼ�������ͬϵ��ְ��䶯
	private SameSeriesDutyChangeService organTechWorkerSameSeriesDutyChangeService;
	//���ʽṹ����
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
	 * ͬϵ��ְ��䶯
	 * @param map(personOid:String��Աid,
	 *            newDutyLevel:String����ְ�񼶱�, leadFlag:Integer�쵼״̬,
	 *            startDate:String��н���� ,wageSerie:String����ϵ��,processInsId:String)
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
      * @param map(personOid:String��Աid,
	 *            newDutyLevel:String����ְ�񼶱�, leadFlag:Integer�쵼״̬,
	 *            startDate:String��н����yyyy-mm-dd )
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
      * @param map(personOid:String, startDate:String��н����yyyy-mm-dd)
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
      * @param map(personOid:String, startDate:String��н����yyyy-mm-dd)
      * @return
      * @throws WageException
      */
     private WageViewDTO organCommonWorkerDutyChange(Map<String,Object> map) throws WageException
     {
    	 //������ͨ������ְ��䶯
    	 WageViewDTO wvd = new WageViewDTO();
    	 UtilHelper.transformObject(wvd);
    	 wvd.setCalulationProInfo("������ͨ������ְ��䶯.");
       	return  wvd; 
     }

	
}

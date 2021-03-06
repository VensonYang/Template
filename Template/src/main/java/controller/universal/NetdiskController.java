package controller.universal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import common.StaticsConstancts;
import controller.base.ControllerContext;
import controller.base.ControllerHelper;
import controller.base.ReturnResult;
import controller.base.StatusCode;
import controller.base.ValidParam;
import controller.base.ValidationAware;
import controller.base.ValidationAwareSupport;
import model.common.QueryVO;
import model.universal.BatchFileVO;
import model.universal.FileVO;
import service.universal.NetdiskService;
import utils.bean.BeanDirectorFactory;
import utils.parse.WordToHtml;

@RequestMapping("/netdisk")
@ResponseBody
@Controller
public class NetdiskController {

	private static final Logger logger = LoggerFactory.getLogger(NetdiskController.class);
	@Autowired
	private NetdiskService netdiskService;

	@RequestMapping("query")
	public ReturnResult query() {
		ReturnResult returnResult = ControllerContext.getResult();
		QueryVO queryVO = BeanDirectorFactory.getBeanDirector().getDataVO(QueryVO.class);
		Map<String, Object> result = netdiskService.queryFiles(queryVO);
		returnResult.setStatus(StatusCode.SUCCESS).setRows(result.get(StaticsConstancts.DATA))
				.setTotal(result.get(StaticsConstancts.TOTAL));
		logger.debug("showQueryAllFiles success");
		return returnResult;
	}

	@RequestMapping(value = "save")
	public ReturnResult save() {
		ReturnResult returnResult = ControllerContext.getResult();
		ValidationAware va = new ValidationAwareSupport();
		BatchFileVO fileVO = BeanDirectorFactory.getBeanDirector().getDataVO(BatchFileVO.class, va);
		if (ControllerHelper.checkError(fileVO, va, returnResult, logger)) {
			return returnResult;
		}
		netdiskService.addBatchFile(fileVO, ControllerHelper.getUserId());
		returnResult.setStatus(StatusCode.SUCCESS);
		return returnResult;
	}

	@RequestMapping("get")
	public ReturnResult get() {
		ReturnResult returnResult = ControllerContext.getResult();
		String param = ControllerHelper.checkParam(ValidParam.NUM);
		if (param == null) {
			return returnResult;
		}
		FileVO file = netdiskService.getFileByFileId(Integer.parseInt(param));
		Map<String, String> result = new HashMap<String, String>();
		result.put("downName", file.getFileName());
		result.put("fileName", file.getFilePath());
		returnResult.setStatus(StatusCode.SUCCESS).setRows(result);
		return returnResult;
	}

	@RequestMapping("delete")
	public ReturnResult delete() {
		ReturnResult returnResult = ControllerContext.getResult();
		String param = ControllerHelper.checkParam(ValidParam.NUM);
		if (param == null) {
			return returnResult;
		}
		netdiskService.deleteFile(Integer.parseInt(param));
		return returnResult;
	}

	@RequestMapping("preview")
	public ReturnResult preview() throws Exception {
		ReturnResult returnResult = ControllerContext.getResult();
		String fileName = ControllerHelper.checkParam(ValidParam.NOT_BLANK, "fileName");
		if (fileName == null) {
			return returnResult;
		}
		String htmlPath = ControllerHelper.getUploadPath(ControllerHelper.HTML_UPLOAD_PATH);
		String wordPath = ControllerHelper.getUploadPath(ControllerHelper.ATTACHMENT_ROOT_PATH);
		logger.debug(wordPath + fileName);
		if (!new File(wordPath + fileName).exists()) {
			returnResult.setStatus(StatusCode.FAIL).setRows("文件不存在！");
			return returnResult;
		}
		String name = fileName.substring(0, fileName.lastIndexOf("."));
		name = name.substring(name.lastIndexOf("-") + 1);
		String position = htmlPath + name + ".html";
		if (!new File(position).exists()) {
			WordToHtml.getInstance().toHtml(wordPath, fileName, htmlPath, name + ".html");

		}
		returnResult.setStatus(StatusCode.SUCCESS).setRows(name + ".html");
		return returnResult;
	}

}

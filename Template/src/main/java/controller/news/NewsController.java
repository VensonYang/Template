package controller.news;

import java.util.Map;

import javax.servlet.http.HttpSession;

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
import model.news.NewsVO;
import model.news.NewsVO.IAddNews;
import model.news.NewsVO.IModifyNews;
import service.news.NewsService;
import utils.bean.BeanDirectorFactory;

@RequestMapping("/news")
@ResponseBody
@Controller
public class NewsController {

	private static final Logger logger = LoggerFactory.getLogger(NewsController.class);

	@Autowired
	private NewsService newsService;

	@RequestMapping("query")
	public ReturnResult query() {
		ReturnResult returnResult = ControllerContext.getResult();
		QueryVO queryVO = BeanDirectorFactory.getBeanDirector().getDataVO(QueryVO.class);
		Map<String, Object> result = newsService.queryNews(queryVO);
		returnResult.setStatus(StatusCode.SUCCESS).setRows(result.get(StaticsConstancts.DATA))
				.setTotal(result.get(StaticsConstancts.TOTAL));
		logger.debug("showQueryFiles success");
		return returnResult;
	}

	@RequestMapping(value = "save")
	public ReturnResult save() {
		ReturnResult returnResult = ControllerContext.getResult();
		ValidationAware va = new ValidationAwareSupport();
		HttpSession session = ControllerContext.getSession();
		NewsVO NewsVO = BeanDirectorFactory.getBeanDirector().getDataVO(NewsVO.class, va, IAddNews.class);
		if (ControllerHelper.checkError(NewsVO, va, returnResult, logger)) {
			return returnResult;
		}
		String publisher = (String) session.getAttribute(StaticsConstancts.USER_NAME);
		NewsVO.setPublisher(publisher);
		newsService.addNews(NewsVO);
		returnResult.setStatus(StatusCode.SUCCESS);
		logger.debug("save Newssuccess");
		return returnResult;
	}

	@RequestMapping(value = "update")
	public ReturnResult update() {
		ReturnResult returnResult = ControllerContext.getResult();
		ValidationAware va = new ValidationAwareSupport();
		NewsVO NewsVO = BeanDirectorFactory.getBeanDirector().getDataVO(NewsVO.class, va, IModifyNews.class);
		if (ControllerHelper.checkError(NewsVO, va, returnResult, logger)) {
			return returnResult;
		}
		newsService.modifyNews(NewsVO);
		returnResult.setStatus(StatusCode.SUCCESS);
		logger.debug("modify Newssuccess");
		return returnResult;
	}

	@RequestMapping(value = "delete")
	public ReturnResult delete() {
		ReturnResult returnResult = ControllerContext.getResult();
		String param = ControllerHelper.checkParam(ValidParam.NUM);
		if (param == null) {
			return returnResult;
		}
		newsService.deleteNews(Integer.parseInt(param));
		returnResult.setStatus(StatusCode.SUCCESS);
		return returnResult;
	}

	@RequestMapping(value = "get")
	public ReturnResult get() {
		ReturnResult returnResult = ControllerContext.getResult();
		String param = ControllerHelper.checkParam(ValidParam.NUM);
		if (param == null) {
			return returnResult;
		}
		returnResult.setStatus(StatusCode.SUCCESS).setRows(newsService.getNewsVOById(Integer.parseInt(param)));
		logger.debug("get Newssuccess");
		return returnResult;
	}

}

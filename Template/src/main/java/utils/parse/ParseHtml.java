package utils.parse;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import controller.base.ControllerHelper;
import model.parse.Item;
import model.parse.Paper;
import model.parse.ParseHtmlVO;
import model.parse.Question;

public class ParseHtml {

	private String filePath;

	private ParseHtml() {
	}

	private static ParseHtml instance = new ParseHtml();

	public static ParseHtml getIntance() {
		return instance;
	}

	/**
	 * 用于匹配小题的正则表达式
	 * 
	 * @param text
	 *            需要匹配的文本
	 * @param headers
	 *            匹配的起始头
	 */
	public boolean matches(String text) {
		if (text.matches(Resolver.REGEX_SMALL_ITEM_START + ".*"))
			return true;
		return false;
	}

	/**
	 * 根据指定的节点进行解析
	 * 
	 * @param children
	 *            节点
	 * @param staIdx
	 *            起始索引
	 * @param length
	 *            长度
	 */
	public Question getQuestions(Elements children, int staIdx, int length, String name) {
		Question question = new Question(name);
		StringBuilder builder = new StringBuilder();
		Item item = new Item();
		item.setQuestion(name);
		StringBuilder path = new StringBuilder();
		boolean isStart = false;
		for (int i = 0; i < length; i++) {
			Element e = children.get(staIdx);
			// 这里可以对文本进行处理
			String text = e.text();
			// 将全角空格替换成半角空格
			text = text.replace((char) 12288, ' ').trim();
			text = text.replace((char) 160, ' ').trim();
			// 过滤字符如：（X分），图X
			text = text.replaceAll("[(（].{0,6}\\d{1,2}分.{0,7}[）)]", "");
			text = text.replaceAll("图\\d{1,2}", "图");
			String html = e.html();
			if (matches(text)) {
				isStart = true;
				if (StringUtils.isNotBlank(builder.toString())) {
					String imgPath = path.toString();
					imgPath = StringUtils.isBlank(imgPath) ? null : imgPath;
					item.setTitle(builder.toString());
					item.setImgPath(imgPath);
					question.add(item);
				}
				builder = new StringBuilder();
				path = new StringBuilder();
				item = new Item();
				item.setQuestion(name);
			}
			if (isStart) {
				builder.append(text);
				builder.append("\n");
			}
			if (html.contains("img")) {
				Elements elems = e.children();
				for (Element em : elems) {
					if (em.nodeName().equals("img")) {
						String src = em.attr("src");
						String imgPath = getRootImagePath(src);
						path.append(imgPath);
						path.append(",");
					}
				}
			}
			staIdx++;
		}
		if (StringUtils.isNotBlank(builder.toString())) {
			String imgPath = path.toString();
			imgPath = StringUtils.isBlank(imgPath) ? null : imgPath;
			item.setTitle(builder.toString());
			item.setImgPath(imgPath);
			question.add(item);
		}
		return question;
	}

	private String getRootImagePath(String src) {
		String imgPath = filePath.substring(ControllerHelper.getUploadPath("/").length());
		imgPath = imgPath.substring(0, imgPath.lastIndexOf("/")).replaceAll("\\\\", "/");
		imgPath = ControllerHelper.getDeployDomain() + imgPath + "/" + src;
		return imgPath;
	}

	/**
	 * 根据指定的节点获取答案区
	 * 
	 * @param children
	 *            节点
	 * @param staIdx
	 *            起始索引
	 * @param length
	 *            长度
	 */
	public Question getAnswer(Elements children, int staIdx, int length) {
		Question question = new Question(Question.ANSWER);
		StringBuilder builder = new StringBuilder();
		Item item = new Item();
		for (int i = 0; i < length; i++) {
			Element e = children.get(staIdx);
			// 这里可以对文本进行处理
			String text = e.text();
			// 将全角空格替换成半角空格
			text = text.replace((char) 12288, ' ').trim();
			text = text.replace((char) 160, ' ').trim();
			// 过滤字符如：（X分），图X
			text = text.replaceAll("[(（].{0,6}\\d{1,2}分.{0,7}[）)]", "");
			text = text.replaceAll("图\\d{1,2}", "图");
			builder.append(text);
			String html = e.html();
			if (html.contains("img")) {
				Elements elems = e.children();
				for (Element em : elems) {
					if (em.nodeName().equals("img")) {
						String src = em.attr("src");
						String imgPath = getRootImagePath(src);
						builder.append(imgPath);
					}
				}
			}
			staIdx++;
		}
		if (StringUtils.isNotBlank(builder.toString())) {
			item.setTitle(builder.toString());
			question.add(item);
		}
		return question;
	}

	/**
	 * 根据指定节点获取题型
	 * 
	 * @param children
	 *            节点
	 */
	public Map<String, String> getType(Elements children) {
		Map<String, String> map = new HashMap<String, String>();
		for (Element e : children) {
			// 从试卷里面查找题型
			String text = e.text();
			if (text.contains(Question.ANSWER)) {
				// 如果文档中包含答案区，则不再往下查找
				map.put(String.valueOf(e.elementSiblingIndex()), Question.ANSWER);
				return map;
			} else {
				Pattern p = Pattern.compile("^" + Resolver.REGEX_BIG_QUESTION);
				Matcher m = p.matcher(text);
				if (m.find()) {
					String name = m.group().replaceAll(Resolver.REGEX_BIG_NUM, "").replace((char) 12288, ' ').trim();
					map.put(String.valueOf(e.elementSiblingIndex()), name);
				}
			}

		}
		if (map.size() == 0) {
			return null;
		}
		return map;
	}

	/**
	 * 根据题型进行索引排序
	 * 
	 * @param map
	 *            题型键值对
	 */
	public String[] sortIndex(Map<String, String> map) {
		if (map == null) {
			return null;
		}
		if (map.size() > 1) {
			String[] result = map.keySet().toArray(new String[] {});
			Arrays.sort(result, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					int i = Integer.parseInt(o1.split(":")[0]);
					int j = Integer.parseInt(o2.split(":")[0]);
					return i - j;
				}
			});
			return result;
		} else {
			return map.keySet().toArray(new String[] {});
		}

	}

	/**
	 * 利用Jsoup组件对html试卷进行解析
	 * 
	 * @param path
	 *            文件路径
	 * @param subject
	 *            科目
	 * @throws Exception
	 */
	public Paper parse(ParseHtmlVO vo) throws Exception {
		filePath = vo.getPath();
		// 定义一个指定科目的试卷保存对象
		Paper paper = new Paper(vo.getSubject());
		// 读取文件
		Document doc = Jsoup.parse(new File(filePath), "utf-8");
		// 获取body标签内的所有子标签
		Element body = doc.body();
		Elements children = body.children();
		// 获取试卷内包含的题型
		Map<String, String> map = getType(children);
		// 将题型索引按升序排序
		String[] array = sortIndex(map);
		if (array == null) {
			return paper.setMessage("解析出错:文档中未找到可以解析的题型");

		}
		Set<String> subjectQuestion = vo.getQuestions();
		for (String name : map.values()) {
			if (!subjectQuestion.contains(name) && !name.equals("***答案区***")) {
				return paper.setMessage("解析出错:题型[" + name + "]未保存！");
			}
		}
		int size = array.length;
		try {
			// 按题型开始解析
			for (int i = 0; i < size; i++) {
				// 当前题型索引
				int curIdx = Integer.valueOf(array[i]);
				// 起始题型索引
				int staIdx = curIdx + 1;
				// 获取该题型的长度，先判断当前题型是否为末尾题型如果不是则：长度=下一题型索引-当前题型索引-1，否则：长度=总长度-当前题型索引-1
				int length = i < size - 1 ? Integer.valueOf(array[i + 1]) - curIdx - 1 : children.size() - curIdx - 1;
				// 匹配已有题型和试卷题型
				Question question;
				String name = map.get(array[i]);
				if (name == Question.ANSWER) {
					question = getAnswer(children, staIdx, length);
				} else {
					question = getQuestions(children, staIdx, length, name);
				}
				paper.add(question);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return paper.setMessage("解析出错:试题格式有误");

		}
		boolean isNull = false;
		for (Question question : paper.getQuestions()) {
			if (!question.getName().equals(Question.ANSWER)) {
				System.out.println(question.getName());
				List<Item> items = question.getItems();
				if (items.size() != 0)
					isNull = true;
			}
		}
		if (!isNull)
			return paper.setMessage("解析出错:文档中未找到可以解析的题型");
		return paper;
	}

}
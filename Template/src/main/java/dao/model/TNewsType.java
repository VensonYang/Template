package dao.model;
// Generated 2016-7-25 10:41:41 by Hibernate Tools 4.3.1.Final

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * TNewsType generated by hbm2java
 */
public class TNewsType implements java.io.Serializable {

	private Integer id;
	private String name;
	private String status;
	private Date createTime;
	private Date modifyTime;
	private int creator;
	private Integer modifier;
	private int sort;
	private String memo;
	private Set TNewses = new HashSet(0);
	private Set TNewses_1 = new HashSet(0);

	public TNewsType() {
	}

	public TNewsType(String name, String status, Date createTime, int creator, int sort) {
		this.name = name;
		this.status = status;
		this.createTime = createTime;
		this.creator = creator;
		this.sort = sort;
	}

	public TNewsType(String name, String status, Date createTime, Date modifyTime, int creator, Integer modifier,
			int sort, String memo, Set TNewses, Set TNewses_1) {
		this.name = name;
		this.status = status;
		this.createTime = createTime;
		this.modifyTime = modifyTime;
		this.creator = creator;
		this.modifier = modifier;
		this.sort = sort;
		this.memo = memo;
		this.TNewses = TNewses;
		this.TNewses_1 = TNewses_1;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifyTime() {
		return this.modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public int getCreator() {
		return this.creator;
	}

	public void setCreator(int creator) {
		this.creator = creator;
	}

	public Integer getModifier() {
		return this.modifier;
	}

	public void setModifier(Integer modifier) {
		this.modifier = modifier;
	}

	public int getSort() {
		return this.sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public String getMemo() {
		return this.memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Set getTNewses() {
		return this.TNewses;
	}

	public void setTNewses(Set TNewses) {
		this.TNewses = TNewses;
	}

	public Set getTNewses_1() {
		return this.TNewses_1;
	}

	public void setTNewses_1(Set TNewses_1) {
		this.TNewses_1 = TNewses_1;
	}

}

package org.williamsun.spring.test.custom.element;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/6/3.
 */
public class User {
	private String id;

	private String userName;

	private String email;

	public void setId(String id) {
		this.id = id;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{");
		sb.append("\"id\":\"")
				.append(id).append('\"');
		sb.append(",\"userName\":\"")
				.append(userName).append('\"');
		sb.append(",\"email\":\"")
				.append(email).append('\"');
		sb.append('}');
		return sb.toString();
	}
}

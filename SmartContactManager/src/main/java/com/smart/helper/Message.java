package com.smart.helper;

import java.io.Serializable;

public class Message implements Serializable{
	
	private static final long serialVersionUID = -1359987151732232047L;
	
	private String content;
	private String type;
	
	public Message() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Message(String content, String type) {
		super();
		this.content = content;
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Message [content=" + content + ", type=" + type + "]";
	}
	
	

}

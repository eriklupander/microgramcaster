package com.squeed.microgramcaster.util;

import java.util.Stack;

public class PathStack {

	private static Stack<String> containerStack = new Stack<String>();
	
	public static void popContainerIdStack() {
		if(containerStack.size() > 0) {
			containerStack.pop();	
		}
	}
	
	public static void clearContainerIdStack() {
		containerStack.clear();
	}
	
	public static Stack<String> get() {
		return containerStack;
	}
	
}

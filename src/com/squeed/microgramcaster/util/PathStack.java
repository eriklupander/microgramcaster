package com.squeed.microgramcaster.util;

import java.util.Stack;

public class PathStack {

	private static Stack<String> containerStack = new Stack<String>();
	
	public static void popContainerIdStack() {
		containerStack.pop();
	}
	
	public static void clearContainerIdStack() {
		containerStack.clear();
	}
	
	public static Stack<String> get() {
		return containerStack;
	}
	
}

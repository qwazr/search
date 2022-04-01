/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwazr.utils;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class WordCount {

	private final HashMap<String, AtomicInteger> wordCount;

	public WordCount() {
		wordCount = new HashMap<>();
	}

	public WordCount(String sentence) {
		this();
		addSentence(sentence);
	}

	public WordCount(String[] sentences) {
		this();
		for (String sentence : sentences)
			addSentence(sentence);
	}

	public WordCount(Collection<String> sentences) {
		this();
		sentences.forEach(sentence -> addSentence(sentence));
	}

	public void addSentence(String sentence) {
		if (sentence == null)
			return;
		String[] words = StringUtils.split(sentence, "\t \n\r");
		for (String word : words)
			addWord(word);
	}

	public int addWord(String word) {
		if (word == null)
			return 0;
		return wordCount.computeIfAbsent(word, k -> new AtomicInteger()).incrementAndGet();
	}

	public static float compare(WordCount dic1, WordCount dic2) {
		final HashSet<String> wordSet = new HashSet<>(dic1.wordCount.keySet());
		wordSet.addAll(dic2.wordCount.keySet());
		final AtomicDouble similarity = new AtomicDouble();
		wordSet.forEach(word -> {
			final AtomicInteger c1 = dic1.wordCount.get(word);
			final AtomicInteger c2 = dic2.wordCount.get(word);
			if (c1 == null || c2 == null)
				return;
			final double v1 = c1.doubleValue();
			final double v2 = c2.doubleValue();
			final double delta = v1 > v2 ? v2 / v1 : v1 / v2;
			similarity.addAndGet(delta);
		});
		return similarity.floatValue() / (float) wordSet.size();
	}

}

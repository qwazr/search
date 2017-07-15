/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwazr.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ar.ArabicNormalizationFilter;
import org.apache.lucene.analysis.bg.BulgarianStemFilter;
import org.apache.lucene.analysis.cjk.CJKBigramFilter;
import org.apache.lucene.analysis.cjk.CJKWidthFilter;
import org.apache.lucene.analysis.core.DecimalDigitFilter;
import org.apache.lucene.analysis.cz.CzechStemFilter;
import org.apache.lucene.analysis.de.GermanLightStemFilter;
import org.apache.lucene.analysis.de.GermanNormalizationFilter;
import org.apache.lucene.analysis.el.GreekLowerCaseFilter;
import org.apache.lucene.analysis.el.GreekStemFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.es.SpanishLightStemFilter;
import org.apache.lucene.analysis.ga.IrishLowerCaseFilter;
import org.apache.lucene.analysis.hi.HindiNormalizationFilter;
import org.apache.lucene.analysis.hi.HindiStemFilter;
import org.apache.lucene.analysis.in.IndicNormalizationFilter;
import org.apache.lucene.analysis.it.ItalianLightStemFilter;
import org.apache.lucene.analysis.lv.LatvianStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.pt.PortugueseLightStemFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tr.ApostropheFilter;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;
import org.apache.lucene.analysis.util.ElisionFilter;
import org.tartarus.snowball.ext.DanishStemmer;
import org.tartarus.snowball.ext.FinnishStemmer;
import org.tartarus.snowball.ext.HungarianStemmer;
import org.tartarus.snowball.ext.IrishStemmer;
import org.tartarus.snowball.ext.LithuanianStemmer;
import org.tartarus.snowball.ext.NorwegianStemmer;
import org.tartarus.snowball.ext.RomanianStemmer;
import org.tartarus.snowball.ext.SwedishStemmer;
import org.tartarus.snowball.ext.TurkishStemmer;

import java.io.Reader;
import java.util.Arrays;

/**
 * A set of analyzer, with language support. without stopwords
 */
public enum SmartAnalyzerSet {

	lowercase(Lowercase.class),
	ascii(Ascii.class),

	arabic(Arabic.class),
	bulgarian(Bulgarian.class),
	cjk(CJK.class),
	czech(Czech.class),
	danish(Danish.class),
	dutch(Dutch.class),
	german(German.class),
	greek(Greek.class),
	english(English.class),
	spanish(Spanish.class),
	finnish(Finnish.class),
	irish(Irish.class),
	hindi(Hindi.class),
	hungarian(Hungarian.class),
	italian(Italian.class),
	lithuanian(Lithuanian.class),
	latvian(Latvian.class),
	norwegian(Norwegian.class),
	portuguese(Portuguese.class),
	romanian(Romanian.class),
	russian(Russian.class),
	swedish(Swedish.class),
	turkish(Turkish.class);

	public final Class<? extends Analyzer> indexAnalyzer;
	public final Class<? extends Analyzer> queryAnalyzer;

	SmartAnalyzerSet(Class<? extends Analyzer> indexAnalyzer, Class<? extends Analyzer> queryAnalyzer) {
		this.indexAnalyzer = indexAnalyzer;
		this.queryAnalyzer = queryAnalyzer;
	}

	SmartAnalyzerSet(Class<? extends Analyzer> analyzer) {
		this(analyzer, analyzer);
	}

	private final static int MAX_TOKEN_LENGTH = 255;
	private final static int POSITION_INCREMENT_GAP = 100;

	static abstract class Common extends Analyzer {

		final public int getPositionIncrementGap(String fieldName) {
			return POSITION_INCREMENT_GAP;
		}

		@Override
		final protected TokenStreamComponents createComponents(final String fieldName) {
			final StandardTokenizer src = new StandardTokenizer();
			src.setMaxTokenLength(MAX_TOKEN_LENGTH);

			TokenStream tok = normalize(fieldName, src);
			return new TokenStreamComponents(src, tok) {
				@Override
				protected void setReader(final Reader reader) {
					src.setMaxTokenLength(MAX_TOKEN_LENGTH);
					super.setReader(reader);
				}
			};
		}
	}

	static public final class Lowercase extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream in) {
			TokenStream result = new StandardFilter(in);
			result = new LowerCaseFilter(result);
			return result;
		}
	}

	static public final class Ascii extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream in) {
			TokenStream result = new StandardFilter(in);
			result = new LowerCaseFilter(result);
			result = new ASCIIFoldingFilter(result);
			return result;
		}
	}

	static public final class Arabic extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream in) {
			TokenStream result = new LowerCaseFilter(in);
			result = new DecimalDigitFilter(result);
			result = new ArabicNormalizationFilter(result);
			return result;
		}
	}

	static public final class Bulgarian extends Common {

		protected TokenStream normalize(String fieldName, TokenStream in) {
			TokenStream result = new StandardFilter(in);
			result = new LowerCaseFilter(result);
			result = new BulgarianStemFilter(result);
			return result;
		}
	}

	static public final class CJK extends Common {

		protected TokenStream normalize(String fieldName, TokenStream in) {
			TokenStream result = new CJKWidthFilter(in);
			result = new LowerCaseFilter(result);
			result = new CJKBigramFilter(result);
			return result;
		}
	}

	static public final class Czech extends Common {

		protected TokenStream normalize(String fieldName, TokenStream in) {
			TokenStream result = new StandardFilter(in);
			result = new LowerCaseFilter(result);
			result = new CzechStemFilter(result);
			return result;
		}
	}

	static public final class Danish extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream in) {
			TokenStream result = new StandardFilter(in);
			result = new LowerCaseFilter(result);
			result = new SnowballFilter(result, new DanishStemmer());
			return result;
		}
	}

	static public final class German extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new LowerCaseFilter(result);
			result = new GermanNormalizationFilter(result);
			result = new GermanLightStemFilter(result);
			return result;
		}
	}

	static public final class Greek extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new GreekLowerCaseFilter(source);
			result = new StandardFilter(result);
			result = new GreekStemFilter(result);
			return result;
		}
	}

	static public final class English extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new EnglishPossessiveFilter(result);
			result = new LowerCaseFilter(result);
			result = new PorterStemFilter(result);
			return result;
		}
	}

	static public final class Spanish extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new LowerCaseFilter(result);
			result = new SpanishLightStemFilter(result);
			return result;
		}
	}

	static public final class Finnish extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new LowerCaseFilter(result);
			result = new SnowballFilter(result, new FinnishStemmer());
			return result;
		}
	}

	static public final class Irish extends Common {

		private static final CharArraySet DEFAULT_ARTICLES = CharArraySet.unmodifiableSet(
				new CharArraySet(Arrays.asList("d", "m", "b"), true));

		private static final CharArraySet HYPHENATIONS = CharArraySet.unmodifiableSet(
				new CharArraySet(Arrays.asList("h", "n", "t"), true));

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new StopFilter(result, HYPHENATIONS);
			result = new ElisionFilter(result, DEFAULT_ARTICLES);
			result = new IrishLowerCaseFilter(result);
			result = new SnowballFilter(result, new IrishStemmer());
			return result;
		}
	}

	static public final class Hindi extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new LowerCaseFilter(source);
			result = new DecimalDigitFilter(result);
			result = new IndicNormalizationFilter(result);
			result = new HindiNormalizationFilter(result);
			result = new HindiStemFilter(result);
			return result;
		}
	}

	static public final class Hungarian extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new LowerCaseFilter(result);
			result = new SnowballFilter(result, new HungarianStemmer());
			return result;
		}
	}

	static public final class Italian extends Common {

		private static final CharArraySet DEFAULT_ARTICLES = CharArraySet.unmodifiableSet(new CharArraySet(
				Arrays.asList("c", "l", "all", "dall", "dell", "nell", "sull", "coll", "pell", "gl", "agl", "dagl",
						"degl", "negl", "sugl", "un", "m", "t", "s", "v", "d"), true));

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new ElisionFilter(result, DEFAULT_ARTICLES);
			result = new LowerCaseFilter(result);
			result = new ItalianLightStemFilter(result);
			return result;
		}
	}

	static public final class Lithuanian extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new LowerCaseFilter(result);
			result = new SnowballFilter(result, new LithuanianStemmer());
			return result;
		}
	}

	static public final class Latvian extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new LowerCaseFilter(result);
			result = new LatvianStemFilter(result);
			return result;
		}
	}

	static public final class Dutch extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new LowerCaseFilter(result);
			result = new SnowballFilter(result, new org.tartarus.snowball.ext.DutchStemmer());
			return result;
		}
	}

	static public final class Norwegian extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new LowerCaseFilter(result);
			result = new SnowballFilter(result, new NorwegianStemmer());
			return result;
		}
	}

	static public final class Portuguese extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new LowerCaseFilter(result);
			result = new PortugueseLightStemFilter(result);
			return result;
		}
	}

	static public final class Romanian extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new LowerCaseFilter(result);
			result = new SnowballFilter(result, new RomanianStemmer());
			return result;
		}
	}

	static public final class Russian extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new LowerCaseFilter(result);
			result = new SnowballFilter(result, new org.tartarus.snowball.ext.RussianStemmer());
			return result;
		}
	}

	static public final class Swedish extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new LowerCaseFilter(result);
			result = new SnowballFilter(result, new SwedishStemmer());
			return result;
		}
	}

	static public final class Turkish extends Common {

		@Override
		protected TokenStream normalize(String fieldName, TokenStream source) {
			TokenStream result = new StandardFilter(source);
			result = new ApostropheFilter(result);
			result = new TurkishLowerCaseFilter(result);
			result = new SnowballFilter(result, new TurkishStemmer());
			return result;
		}
	}

}
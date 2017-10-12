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
import org.apache.lucene.analysis.Tokenizer;
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
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.fr.FrenchLightStemFilter;
import org.apache.lucene.analysis.ga.IrishLowerCaseFilter;
import org.apache.lucene.analysis.hi.HindiNormalizationFilter;
import org.apache.lucene.analysis.hi.HindiStemFilter;
import org.apache.lucene.analysis.in.IndicNormalizationFilter;
import org.apache.lucene.analysis.it.ItalianLightStemFilter;
import org.apache.lucene.analysis.lv.LatvianStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseLightStemFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.UAX29URLEmailTokenizer;
import org.apache.lucene.analysis.stempel.StempelFilter;
import org.apache.lucene.analysis.stempel.StempelStemmer;
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
 * A set of analyzer, with language support
 */
public enum SmartAnalyzerSet {

	LOWERCASE(LowercaseIndex.class, LowercaseQuery.class),
	ASCII(AsciiIndex.class, AsciiQuery.class),
	ARABIC(ArabicIndex.class, ArabicQuery.class),
	BULGARIAN(BulgarianIndex.class, BulgarianQuery.class),
	CJK(CJKIndex.class, CJKQuery.class),
	CZECH(CzechIndex.class, CzechQuery.class),
	DANISH(DanishIndex.class, DanishQuery.class),
	DUTCH(DutchIndex.class, DutchQuery.class),
	FRENCH(FrenchIndex.class, FrenchQuery.class),
	GERMAN(GermanIndex.class, GermanQuery.class),
	GREEK(GreekIndex.class, GreekQuery.class),
	ENGLISH(EnglishIndex.class, EnglishQuery.class),
	FINNISH(FinnishIndex.class, FinnishQuery.class),
	IRISH(IrishIndex.class, IrishQuery.class),
	HINDI(HindiIndex.class, HindiQuery.class),
	HUNGARIAN(HungarianIndex.class, HungarianQuery.class),
	ITALIAN(ItalianIndex.class, ItalianQuery.class),
	LITHUANIAN(LithuanianIndex.class, LithuanianQuery.class),
	LATVIAN(LatvianIndex.class, LatvianQuery.class),
	NORWEGIAN(NorwegianIndex.class, NorwegianQuery.class),
	POLISH(PolishIndex.class, PolishQuery.class),
	PORTUGUESE(PortugueseIndex.class, PortugueseQuery.class),
	ROMANIAN(RomanianIndex.class, RomanianQuery.class),
	RUSSIAN(RussianIndex.class, RussianQuery.class),
	SPANISH(SpanishIndex.class, SpanishQuery.class),
	SWEDISH(SwedishIndex.class, SwedishQuery.class),
	TURKISH(TurkishIndex.class, TurkishQuery.class);

	public final Class<? extends Analyzer> indexAnalyzer;
	public final Class<? extends Analyzer> queryAnalyzer;

	SmartAnalyzerSet(Class<? extends Index> indexAnalyzer, Class<? extends Query> queryAnalyzer) {
		this.indexAnalyzer = indexAnalyzer;
		this.queryAnalyzer = queryAnalyzer;
	}

	public final static int MAX_TOKEN_LENGTH = 255;
	public final static int POSITION_INCREMENT_GAP = 100;

	public static abstract class Common extends Analyzer {

		final private Normalize normalize;
		final private AfterTokenize afterTokenize;

		final public int getPositionIncrementGap(String fieldName) {
			return POSITION_INCREMENT_GAP;
		}

		protected Common(Normalize normalize, AfterTokenize afterTokenize) {
			this.normalize = normalize;
			this.afterTokenize = afterTokenize;
		}

		@Override
		final protected TokenStreamComponents createComponents(final String fieldName) {

			final UAX29URLEmailTokenizer src = new UAX29URLEmailTokenizer();
			src.setMaxTokenLength(MAX_TOKEN_LENGTH);
			final TokenStream tok = normalize.apply(afterTokenize.apply(src));
			return new TokenStreamComponents(src, tok) {
				@Override
				protected void setReader(final Reader reader) {
					src.setMaxTokenLength(MAX_TOKEN_LENGTH);
					super.setReader(reader);
				}
			};
		}
	}

	@FunctionalInterface
	public interface Normalize {
		TokenStream apply(TokenStream in);
	}

	@FunctionalInterface
	public interface AfterTokenize {
		TokenStream apply(Tokenizer src);
	}

	static public TokenStream indexWordDelimiter(Tokenizer src) {
		return new WordDelimiterGraphFilter(src,
				WordDelimiterGraphFilter.GENERATE_WORD_PARTS | WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS |
						WordDelimiterGraphFilter.SPLIT_ON_NUMERICS | WordDelimiterGraphFilter.SPLIT_ON_CASE_CHANGE |
						WordDelimiterGraphFilter.CATENATE_ALL | WordDelimiterGraphFilter.CATENATE_NUMBERS |
						WordDelimiterGraphFilter.CATENATE_WORDS | WordDelimiterGraphFilter.PRESERVE_ORIGINAL,
				CharArraySet.EMPTY_SET);
	}

	public static abstract class Index extends Common {

		protected Index(Normalize normalize) {
			super(normalize, SmartAnalyzerSet::indexWordDelimiter);
		}

	}

	public static TokenStream queryWordDelimiter(Tokenizer src) {
		return new WordDelimiterGraphFilter(src,
				WordDelimiterGraphFilter.GENERATE_WORD_PARTS | WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS |
						WordDelimiterGraphFilter.SPLIT_ON_NUMERICS | WordDelimiterGraphFilter.SPLIT_ON_CASE_CHANGE,
				CharArraySet.EMPTY_SET);
	}

	public static abstract class Query extends Common {

		protected Query(Normalize normalize) {
			super(normalize, SmartAnalyzerSet::queryWordDelimiter);
		}

	}

	static public TokenStream lower(TokenStream in) {
		return new LowerCaseFilter(in);
	}

	static public final class LowercaseIndex extends Index {
		public LowercaseIndex() {
			super(SmartAnalyzerSet::lower);
		}
	}

	static public final class LowercaseQuery extends Query {
		public LowercaseQuery() {
			super(SmartAnalyzerSet::lower);
		}
	}

	static public TokenStream ascii(TokenStream in) {
		return new ASCIIFoldingFilter(new LowerCaseFilter(in));
	}

	static public final class AsciiIndex extends Index {
		public AsciiIndex() {
			super(SmartAnalyzerSet::ascii);
		}
	}

	static public final class AsciiQuery extends Query {
		public AsciiQuery() {
			super(SmartAnalyzerSet::ascii);
		}
	}

	static public TokenStream arabic(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new DecimalDigitFilter(result);
		result = new ArabicNormalizationFilter(result);
		return result;
	}

	static public final class ArabicIndex extends Index {
		public ArabicIndex() {
			super(SmartAnalyzerSet::arabic);
		}
	}

	static public final class ArabicQuery extends Query {
		public ArabicQuery() {
			super(SmartAnalyzerSet::arabic);
		}
	}

	static public TokenStream bulgarian(TokenStream result) {
		result = new StandardFilter(result);
		result = new LowerCaseFilter(result);
		result = new BulgarianStemFilter(result);
		return result;
	}

	static public final class BulgarianIndex extends Index {
		public BulgarianIndex() {
			super(SmartAnalyzerSet::bulgarian);
		}
	}

	static public final class BulgarianQuery extends Query {
		public BulgarianQuery() {
			super(SmartAnalyzerSet::bulgarian);
		}
	}

	static public TokenStream cjk(TokenStream result) {
		result = new CJKWidthFilter(result);
		result = new LowerCaseFilter(result);
		result = new CJKBigramFilter(result);
		return result;
	}

	static public final class CJKIndex extends Index {
		public CJKIndex() {
			super(SmartAnalyzerSet::cjk);
		}
	}

	static public final class CJKQuery extends Query {
		public CJKQuery() {
			super(SmartAnalyzerSet::cjk);
		}
	}

	static public TokenStream czech(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new CzechStemFilter(result);
		return result;
	}

	static public final class CzechIndex extends Index {
		public CzechIndex() {
			super(SmartAnalyzerSet::czech);
		}
	}

	static public final class CzechQuery extends Query {
		public CzechQuery() {
			super(SmartAnalyzerSet::czech);
		}
	}

	static public TokenStream danish(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new SnowballFilter(result, new DanishStemmer());
		return result;
	}

	static public final class DanishIndex extends Index {
		public DanishIndex() {
			super(SmartAnalyzerSet::danish);
		}
	}

	static public final class DanishQuery extends Query {
		public DanishQuery() {
			super(SmartAnalyzerSet::danish);
		}
	}

	static public TokenStream dutch(TokenStream result) {
		result = new StandardFilter(result);
		result = new LowerCaseFilter(result);
		result = new SnowballFilter(result, new org.tartarus.snowball.ext.DutchStemmer());
		return result;
	}

	static public final class DutchIndex extends Index {
		public DutchIndex() {
			super(SmartAnalyzerSet::dutch);
		}
	}

	static public final class DutchQuery extends Query {
		public DutchQuery() {
			super(SmartAnalyzerSet::dutch);
		}
	}

	static public TokenStream english(TokenStream result) {
		result = new EnglishPossessiveFilter(result);
		result = new LowerCaseFilter(result);
		result = new PorterStemFilter(result);
		return result;
	}

	static public final class EnglishIndex extends Index {
		public EnglishIndex() {
			super(SmartAnalyzerSet::english);
		}
	}

	static public final class EnglishQuery extends Query {
		public EnglishQuery() {
			super(SmartAnalyzerSet::english);
		}
	}

	static public TokenStream french(TokenStream result) {
		result = new ElisionFilter(result, FrenchAnalyzer.DEFAULT_ARTICLES);
		result = new LowerCaseFilter(result);
		result = new FrenchLightStemFilter(result);
		return result;
	}

	static public final class FrenchIndex extends Index {
		public FrenchIndex() {
			super(SmartAnalyzerSet::french);
		}
	}

	static public final class FrenchQuery extends Query {
		public FrenchQuery() {
			super(SmartAnalyzerSet::french);
		}
	}

	static public TokenStream finnish(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new SnowballFilter(result, new FinnishStemmer());
		return result;
	}

	static public final class FinnishIndex extends Index {
		public FinnishIndex() {
			super(SmartAnalyzerSet::finnish);
		}
	}

	static public final class FinnishQuery extends Query {
		public FinnishQuery() {
			super(SmartAnalyzerSet::finnish);
		}
	}

	static public TokenStream german(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new GermanNormalizationFilter(result);
		result = new GermanLightStemFilter(result);
		return result;
	}

	static public final class GermanIndex extends Index {
		public GermanIndex() {
			super(SmartAnalyzerSet::german);
		}
	}

	static public final class GermanQuery extends Query {
		public GermanQuery() {
			super(SmartAnalyzerSet::german);
		}
	}

	static public TokenStream greek(TokenStream result) {
		result = new GreekLowerCaseFilter(result);
		result = new GreekStemFilter(result);
		return result;
	}

	static public final class GreekIndex extends Index {
		public GreekIndex() {
			super(SmartAnalyzerSet::greek);
		}
	}

	static public final class GreekQuery extends Query {
		public GreekQuery() {
			super(SmartAnalyzerSet::greek);
		}
	}

	static public TokenStream hindi(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new DecimalDigitFilter(result);
		result = new IndicNormalizationFilter(result);
		result = new HindiNormalizationFilter(result);
		result = new HindiStemFilter(result);
		return result;
	}

	static public final class HindiIndex extends Index {
		public HindiIndex() {
			super(SmartAnalyzerSet::hindi);
		}
	}

	static public final class HindiQuery extends Query {
		public HindiQuery() {
			super(SmartAnalyzerSet::hindi);
		}
	}

	static public TokenStream hungarian(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new SnowballFilter(result, new HungarianStemmer());
		return result;
	}

	static public final class HungarianIndex extends Index {
		public HungarianIndex() {
			super(SmartAnalyzerSet::hungarian);
		}
	}

	static public final class HungarianQuery extends Query {
		public HungarianQuery() {
			super(SmartAnalyzerSet::hungarian);
		}
	}

	static public final CharArraySet IRISH_DEFAULT_ARTICLES =
			CharArraySet.unmodifiableSet(new CharArraySet(Arrays.asList("d", "m", "b"), true));

	static public final CharArraySet IRISH_HYPHENATIONS =
			CharArraySet.unmodifiableSet(new CharArraySet(Arrays.asList("h", "n", "t"), true));

	static public TokenStream irish(TokenStream result) {
		result = new StopFilter(result, IRISH_HYPHENATIONS);
		result = new ElisionFilter(result, IRISH_DEFAULT_ARTICLES);
		result = new IrishLowerCaseFilter(result);
		result = new SnowballFilter(result, new IrishStemmer());
		return result;
	}

	static public final class IrishIndex extends Index {
		public IrishIndex() {
			super(SmartAnalyzerSet::irish);
		}
	}

	static public final class IrishQuery extends Query {
		public IrishQuery() {
			super(SmartAnalyzerSet::irish);
		}
	}

	static public final CharArraySet ITALIAN_DEFAULT_ARTICLES = CharArraySet.unmodifiableSet(new CharArraySet(
			Arrays.asList("c", "l", "all", "dall", "dell", "nell", "sull", "coll", "pell", "gl", "agl", "dagl", "degl",
					"negl", "sugl", "un", "m", "t", "s", "v", "d"), true));

	static public TokenStream italian(TokenStream result) {
		result = new ElisionFilter(result, ITALIAN_DEFAULT_ARTICLES);
		result = new LowerCaseFilter(result);
		result = new ItalianLightStemFilter(result);
		return result;
	}

	static public final class ItalianIndex extends Index {
		public ItalianIndex() {
			super(SmartAnalyzerSet::italian);
		}
	}

	static public final class ItalianQuery extends Query {
		public ItalianQuery() {
			super(SmartAnalyzerSet::italian);
		}
	}

	static public TokenStream lithuanian(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new SnowballFilter(result, new LithuanianStemmer());
		return result;
	}

	static public final class LithuanianIndex extends Index {
		public LithuanianIndex() {
			super(SmartAnalyzerSet::lithuanian);
		}
	}

	static public final class LithuanianQuery extends Query {
		public LithuanianQuery() {
			super(SmartAnalyzerSet::lithuanian);
		}
	}

	static public TokenStream latvian(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new LatvianStemFilter(result);
		return result;
	}

	static public final class LatvianIndex extends Index {
		public LatvianIndex() {
			super(SmartAnalyzerSet::latvian);
		}
	}

	static public final class LatvianQuery extends Query {
		public LatvianQuery() {
			super(SmartAnalyzerSet::latvian);
		}
	}

	static public TokenStream norwegian(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new SnowballFilter(result, new NorwegianStemmer());
		return result;
	}

	static public final class NorwegianIndex extends Index {
		public NorwegianIndex() {
			super(SmartAnalyzerSet::norwegian);
		}
	}

	static public final class NorwegianQuery extends Query {
		public NorwegianQuery() {
			super(SmartAnalyzerSet::norwegian);
		}
	}

	static public TokenStream polish(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new StempelFilter(result, new StempelStemmer(PolishAnalyzer.getDefaultTable()));
		return result;
	}

	static public final class PolishIndex extends Index {
		public PolishIndex() {
			super(SmartAnalyzerSet::polish);
		}
	}

	static public final class PolishQuery extends Query {
		public PolishQuery() {
			super(SmartAnalyzerSet::polish);
		}
	}

	static public TokenStream portuguese(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new PortugueseLightStemFilter(result);
		return result;
	}

	static public final class PortugueseIndex extends Index {
		public PortugueseIndex() {
			super(SmartAnalyzerSet::portuguese);
		}
	}

	static public final class PortugueseQuery extends Query {
		public PortugueseQuery() {
			super(SmartAnalyzerSet::portuguese);
		}
	}

	static public TokenStream romanian(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new SnowballFilter(result, new RomanianStemmer());
		return result;
	}

	static public final class RomanianIndex extends Index {
		public RomanianIndex() {
			super(SmartAnalyzerSet::romanian);
		}
	}

	static public final class RomanianQuery extends Query {
		public RomanianQuery() {
			super(SmartAnalyzerSet::romanian);
		}
	}

	static public TokenStream russian(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new SnowballFilter(result, new org.tartarus.snowball.ext.RussianStemmer());
		return result;
	}

	static public final class RussianIndex extends Index {
		public RussianIndex() {
			super(SmartAnalyzerSet::russian);
		}
	}

	static public final class RussianQuery extends Query {
		public RussianQuery() {
			super(SmartAnalyzerSet::russian);
		}
	}

	static public TokenStream spanish(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new SpanishLightStemFilter(result);
		return result;
	}

	static public final class SpanishIndex extends Index {
		public SpanishIndex() {
			super(SmartAnalyzerSet::spanish);
		}
	}

	static public final class SpanishQuery extends Query {
		public SpanishQuery() {
			super(SmartAnalyzerSet::spanish);
		}
	}

	static public TokenStream swedish(TokenStream result) {
		result = new LowerCaseFilter(result);
		result = new SnowballFilter(result, new SwedishStemmer());
		return result;
	}

	static public final class SwedishIndex extends Index {
		public SwedishIndex() {
			super(SmartAnalyzerSet::swedish);
		}
	}

	static public final class SwedishQuery extends Query {
		public SwedishQuery() {
			super(SmartAnalyzerSet::swedish);
		}
	}

	static public TokenStream turkish(TokenStream result) {
		result = new ApostropheFilter(result);
		result = new TurkishLowerCaseFilter(result);
		result = new SnowballFilter(result, new TurkishStemmer());
		return result;
	}

	static public final class TurkishIndex extends Index {
		public TurkishIndex() {
			super(SmartAnalyzerSet::turkish);
		}
	}

	static public final class TurkishQuery extends Query {
		public TurkishQuery() {
			super(SmartAnalyzerSet::turkish);
		}
	}
}
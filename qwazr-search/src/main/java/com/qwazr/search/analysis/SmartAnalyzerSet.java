/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
import org.apache.lucene.analysis.core.KeywordAnalyzer;
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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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

import java.util.Arrays;

/**
 * A set of analyzer, with language support
 */
public enum SmartAnalyzerSet {

    keyword(KeywordAnalyzer.class),
    standard(StandardAnalyzer.class),
    lowercase(LowercaseIndex.class, LowercaseQuery.class),
    ascii(AsciiIndex.class, AsciiQuery.class),
    arabic(ArabicIndex.class, ArabicQuery.class),
    bulgarian(BulgarianIndex.class, BulgarianQuery.class),
    cjk(CJKIndex.class, CJKQuery.class),
    czech(CzechIndex.class, CzechQuery.class),
    danish(DanishIndex.class, DanishQuery.class),
    dutch(DutchIndex.class, DutchQuery.class),
    english(EnglishIndex.class, EnglishQuery.class),
    french(FrenchIndex.class, FrenchQuery.class),
    german(GermanIndex.class, GermanQuery.class),
    greek(GreekIndex.class, GreekQuery.class),
    finnish(FinnishIndex.class, FinnishQuery.class),
    hindi(HindiIndex.class, HindiQuery.class),
    hungarian(HungarianIndex.class, HungarianQuery.class),
    irish(IrishIndex.class, IrishQuery.class),
    italian(ItalianIndex.class, ItalianQuery.class),
    lithuanian(LithuanianIndex.class, LithuanianQuery.class),
    latvian(LatvianIndex.class, LatvianQuery.class),
    norwegian(NorwegianIndex.class, NorwegianQuery.class),
    polish(PolishIndex.class, PolishQuery.class),
    portuguese(PortugueseIndex.class, PortugueseQuery.class),
    romanian(RomanianIndex.class, RomanianQuery.class),
    russian(RussianIndex.class, RussianQuery.class),
    spanish(SpanishIndex.class, SpanishQuery.class),
    swedish(SwedishIndex.class, SwedishQuery.class),
    turkish(TurkishIndex.class, TurkishQuery.class);

    public final Class<? extends Analyzer> commonAnalyzer;
    public final Class<? extends Analyzer> indexAnalyzer;
    public final Class<? extends Analyzer> queryAnalyzer;

    SmartAnalyzerSet(Class<? extends Analyzer> commonAnalyzer) {
        this.commonAnalyzer = commonAnalyzer;
        this.indexAnalyzer = null;
        this.queryAnalyzer = null;
    }

    SmartAnalyzerSet(Class<? extends Analyzer> indexAnalyzer, Class<? extends Analyzer> queryAnalyzer) {
        this.commonAnalyzer = null;
        this.indexAnalyzer = indexAnalyzer;
        this.queryAnalyzer = queryAnalyzer;
    }

    public Class<? extends Analyzer> forQuery() {
        return queryAnalyzer != null ? queryAnalyzer : commonAnalyzer;
    }

    public Class<? extends Analyzer> forIndex() {
        return indexAnalyzer != null ? indexAnalyzer : commonAnalyzer;
    }

    public final static int MAX_TOKEN_LENGTH = 255;
    public final static int POSITION_INCREMENT_GAP = 100;

    public static SmartAnalyzerSet of(final String analyzerName) {
        try {
            return SmartAnalyzerSet.valueOf(analyzerName);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static abstract class Base extends Analyzer {

        final public int getPositionIncrementGap(String fieldName) {
            return POSITION_INCREMENT_GAP;
        }

        @Override
        final protected TokenStreamComponents createComponents(final String fieldName) {

            final UAX29URLEmailTokenizer src = new UAX29URLEmailTokenizer();
            src.setMaxTokenLength(MAX_TOKEN_LENGTH);
            return new TokenStreamComponents(src, filter(fieldName, src));
        }

        protected TokenStream filter(String fieldName, TokenStream in) {
            return normalize(fieldName, in);
        }

    }

    static public TokenStream indexWordDelimiter(TokenStream src) {
        return new WordDelimiterGraphFilter(src,
            WordDelimiterGraphFilter.GENERATE_WORD_PARTS | WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS |
                WordDelimiterGraphFilter.SPLIT_ON_NUMERICS | WordDelimiterGraphFilter.SPLIT_ON_CASE_CHANGE |
                WordDelimiterGraphFilter.CATENATE_ALL | WordDelimiterGraphFilter.CATENATE_NUMBERS |
                WordDelimiterGraphFilter.CATENATE_WORDS | WordDelimiterGraphFilter.PRESERVE_ORIGINAL,
            CharArraySet.EMPTY_SET);
    }

    public static abstract class Index extends Base {
        protected TokenStream filter(String fieldName, TokenStream in) {
            return normalize(fieldName, indexWordDelimiter(in));
        }
    }

    public static abstract class PayloadBoost extends Index {
        protected TokenStream filter(String fieldName, TokenStream in) {
            final FirstTokenPayloadFilter firstTokenPayloadFilter = new FirstTokenPayloadFilter(in);
            return firstTokenPayloadFilter.newSetterFilter(
                normalize(fieldName, indexWordDelimiter(firstTokenPayloadFilter)));
        }
    }

    public static TokenStream queryWordDelimiter(TokenStream src) {
        return new WordDelimiterGraphFilter(src,
            WordDelimiterGraphFilter.GENERATE_WORD_PARTS | WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS |
                WordDelimiterGraphFilter.SPLIT_ON_NUMERICS | WordDelimiterGraphFilter.SPLIT_ON_CASE_CHANGE,
            CharArraySet.EMPTY_SET);
    }

    public static abstract class Query extends Base {
        protected TokenStream filter(String fieldName, TokenStream in) {
            return normalize(fieldName, queryWordDelimiter(in));
        }
    }

    static public TokenStream lower(TokenStream in) {
        return new LowerCaseFilter(in);
    }

    static public final class LowercaseQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return lower(in);
        }
    }

    static public final class LowercaseIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return lower(in);
        }
    }

    static public TokenStream ascii(TokenStream in) {
        return new ASCIIFoldingFilter(new LowerCaseFilter(in));
    }

    static public final class AsciiIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return ascii(in);
        }
    }

    static public final class AsciiQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return ascii(in);
        }
    }

    static public TokenStream arabic(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new DecimalDigitFilter(result);
        result = new ArabicNormalizationFilter(result);
        return result;
    }

    static public final class ArabicIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return arabic(in);
        }
    }

    static public final class ArabicQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return arabic(in);
        }
    }

    static public TokenStream bulgarian(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new BulgarianStemFilter(result);
        return result;
    }

    static public final class BulgarianIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return bulgarian(in);
        }
    }

    static public final class BulgarianQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return bulgarian(in);
        }
    }

    static public TokenStream cjk(TokenStream result) {
        result = new CJKWidthFilter(result);
        result = new LowerCaseFilter(result);
        result = new CJKBigramFilter(result);
        return result;
    }

    static public final class CJKIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return cjk(in);
        }
    }

    static public final class CJKQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return cjk(in);
        }
    }

    static public TokenStream czech(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new CzechStemFilter(result);
        return result;
    }

    static public final class CzechIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return czech(in);
        }
    }

    static public final class CzechQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return czech(in);
        }
    }

    static public TokenStream danish(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new SnowballFilter(result, new DanishStemmer());
        return result;
    }

    static public final class DanishIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return danish(in);
        }
    }

    static public final class DanishQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return danish(in);
        }
    }

    static public TokenStream dutch(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new SnowballFilter(result, new org.tartarus.snowball.ext.DutchStemmer());
        return result;
    }

    static public final class DutchIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return dutch(in);
        }
    }

    static public final class DutchQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return dutch(in);
        }
    }

    static public TokenStream english(TokenStream result) {
        result = new EnglishPossessiveFilter(result);
        result = new LowerCaseFilter(result);
        result = new PorterStemFilter(result);
        return result;
    }

    static public final class EnglishIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return english(in);
        }
    }

    static public final class EnglishQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return english(in);
        }
    }

    static public TokenStream french(TokenStream result) {
        result = new ElisionFilter(result, FrenchAnalyzer.DEFAULT_ARTICLES);
        result = new LowerCaseFilter(result);
        result = new FrenchLightStemFilter(result);
        return result;
    }

    static public final class FrenchIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return french(in);
        }
    }

    static public final class FrenchQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return french(in);
        }
    }

    static public TokenStream finnish(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new SnowballFilter(result, new FinnishStemmer());
        return result;
    }

    static public final class FinnishIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return finnish(in);
        }
    }

    static public final class FinnishQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return finnish(in);
        }
    }

    static public TokenStream german(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new GermanNormalizationFilter(result);
        result = new GermanLightStemFilter(result);
        return result;
    }

    static public final class GermanIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return german(in);
        }
    }

    static public final class GermanQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return german(in);
        }
    }

    static public TokenStream greek(TokenStream result) {
        result = new GreekLowerCaseFilter(result);
        result = new GreekStemFilter(result);
        return result;
    }

    static public final class GreekIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return greek(in);
        }
    }

    static public final class GreekQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return greek(in);
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
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return hindi(in);
        }
    }

    static public final class HindiQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return hindi(in);
        }
    }

    static public TokenStream hungarian(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new SnowballFilter(result, new HungarianStemmer());
        return result;
    }

    static public final class HungarianIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return hungarian(in);
        }
    }

    static public final class HungarianQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return hungarian(in);
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
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return irish(in);
        }
    }

    static public final class IrishQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return irish(in);
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
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return italian(in);
        }
    }

    static public final class ItalianQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return italian(in);
        }
    }

    static public TokenStream lithuanian(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new SnowballFilter(result, new LithuanianStemmer());
        return result;
    }

    static public final class LithuanianIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return lithuanian(in);
        }
    }

    static public final class LithuanianQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return lithuanian(in);
        }
    }

    static public TokenStream latvian(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new LatvianStemFilter(result);
        return result;
    }

    static public final class LatvianIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return latvian(in);
        }
    }

    static public final class LatvianQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return latvian(in);
        }
    }

    static public TokenStream norwegian(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new SnowballFilter(result, new NorwegianStemmer());
        return result;
    }

    static public final class NorwegianIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return norwegian(in);
        }
    }

    static public final class NorwegianQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return norwegian(in);
        }
    }

    static public TokenStream polish(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new StempelFilter(result, new StempelStemmer(PolishAnalyzer.getDefaultTable()));
        return result;
    }

    static public final class PolishIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return polish(in);
        }
    }

    static public final class PolishQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return polish(in);
        }
    }

    static public TokenStream portuguese(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new PortugueseLightStemFilter(result);
        return result;
    }

    static public final class PortugueseIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return portuguese(in);
        }
    }

    static public final class PortugueseQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return portuguese(in);
        }
    }

    static public TokenStream romanian(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new SnowballFilter(result, new RomanianStemmer());
        return result;
    }

    static public final class RomanianIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return romanian(in);
        }
    }

    static public final class RomanianQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return romanian(in);
        }
    }

    static public TokenStream russian(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new SnowballFilter(result, new org.tartarus.snowball.ext.RussianStemmer());
        return result;
    }

    static public final class RussianIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return russian(in);
        }
    }

    static public final class RussianQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return russian(in);
        }
    }

    static public TokenStream spanish(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new SpanishLightStemFilter(result);
        return result;
    }

    static public final class SpanishIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return spanish(in);
        }
    }

    static public final class SpanishQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return spanish(in);
        }
    }

    static public TokenStream swedish(TokenStream result) {
        result = new LowerCaseFilter(result);
        result = new SnowballFilter(result, new SwedishStemmer());
        return result;
    }

    static public final class SwedishIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return swedish(in);
        }
    }

    static public final class SwedishQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return swedish(in);
        }
    }

    static public TokenStream turkish(TokenStream result) {
        result = new ApostropheFilter(result);
        result = new TurkishLowerCaseFilter(result);
        result = new SnowballFilter(result, new TurkishStemmer());
        return result;
    }

    static public final class TurkishIndex extends Index {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return turkish(in);
        }
    }

    static public final class TurkishQuery extends Query {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return turkish(in);
        }
    }
}

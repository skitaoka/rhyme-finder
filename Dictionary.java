import java.net.URL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

final class Dictionary {
  private static final String EXTENSION = ".dic";

  public static final String[] NAMES = {
    "名詞・一般",
    "名詞・サ変接続",
    "名詞・ナイ形容詞語幹",
    "名詞・形容動詞語幹",
    "動詞・自立・サ変・－スル",
    "動詞・自立・サ変・－ズル",
    "動詞・自立・一段",
    "動詞・自立・五段・カ行イ音便",
    "動詞・自立・五段・カ行促音便",
    "動詞・自立・五段・カ行促音便ユク",
    "動詞・自立・五段・ガ行",
    "動詞・自立・五段・サ行",
    "動詞・自立・五段・タ行",
    "動詞・自立・五段・ナ行",
    "動詞・自立・五段・バ行",
    "動詞・自立・五段・マ行",
    "動詞・自立・五段・ラ行",
    "動詞・自立・五段・ワ行ウ音便",
    "動詞・自立・五段・ワ行促音便",
    "動詞・非自立・一段",
    "動詞・非自立・五段・カ行イ音便",
    "動詞・非自立・五段・サ行",
    "動詞・非自立・五段・マ行",
    "動詞・非自立・五段・ラ行",
    "動詞・非自立・五段・ラ行特殊",
    "動詞・非自立・五段・ワ行促音便",
    "形容詞・アウオ段",
    "形容詞・イ段",
    "副詞・一般",
    "副詞・助詞類接続",
    "連体詞",
    "ユーザー定義",
  };

  // 名詞
  public static final int MASK_NOUM_NONE             = 0x00000001; // 一般
  public static final int MASK_NOUM_SURU             = 0x00000002; // サ変接続
  public static final int MASK_NOUM_NAI              = 0x00000004; // ナイ形容詞語幹
  public static final int MASK_NOUM_DA               = 0x00000008; // 形容動詞語幹
  public static final int MASK_NOUM                  = MASK_NOUM_NONE
                                                     | MASK_NOUM_SURU
                                                     | MASK_NOUM_NAI
                                                     | MASK_NOUM_DA;

  // 動詞
  public static final int MASK_VERB_INDEPENDENT_SURU = 0x00000010; // 自立・サ変・－スル
  public static final int MASK_VERB_INDEPENDENT_ZURU = 0x00000020; // 自立・サ変・－ズル
  public static final int MASK_VERB_INDEPENDENT_1    = 0x00000040; // 自立・一段
  public static final int MASK_VERB_INDEPENDENT_5Ki  = 0x00000080; // 自立・五段・カ行イ音便
  public static final int MASK_VERB_INDEPENDENT_5K_  = 0x00000100; // 自立・五段・カ行促音便
  public static final int MASK_VERB_INDEPENDENT_5Ky  = 0x00000200; // 自立・五段・カ行促音便ユク
  public static final int MASK_VERB_INDEPENDENT_5G   = 0x00000400; // 自立・五段・ガ行
  public static final int MASK_VERB_INDEPENDENT_5S   = 0x00000800; // 自立・五段・サ行
  public static final int MASK_VERB_INDEPENDENT_5T   = 0x00001000; // 自立・五段・タ行
  public static final int MASK_VERB_INDEPENDENT_5N   = 0x00002000; // 自立・五段・ナ行
  public static final int MASK_VERB_INDEPENDENT_5B   = 0x00004000; // 自立・五段・バ行
  public static final int MASK_VERB_INDEPENDENT_5M   = 0x00008000; // 自立・五段・マ行
  public static final int MASK_VERB_INDEPENDENT_5R   = 0x00010000; // 自立・五段・ラ行
  public static final int MASK_VERB_INDEPENDENT_5Wu  = 0x00020000; // 自立・五段・ワ行ウ音便
  public static final int MASK_VERB_INDEPENDENT_5W_  = 0x00040000; // 自立・五段・ワ行促音便
  public static final int MASK_VERB_INDEPENDENT      = MASK_VERB_INDEPENDENT_SURU
                                                     | MASK_VERB_INDEPENDENT_ZURU
                                                     | MASK_VERB_INDEPENDENT_1
                                                     | MASK_VERB_INDEPENDENT_5Ki
                                                     | MASK_VERB_INDEPENDENT_5K_
                                                     | MASK_VERB_INDEPENDENT_5Ky
                                                     | MASK_VERB_INDEPENDENT_5G
                                                     | MASK_VERB_INDEPENDENT_5S
                                                     | MASK_VERB_INDEPENDENT_5T
                                                     | MASK_VERB_INDEPENDENT_5N
                                                     | MASK_VERB_INDEPENDENT_5B
                                                     | MASK_VERB_INDEPENDENT_5M
                                                     | MASK_VERB_INDEPENDENT_5R
                                                     | MASK_VERB_INDEPENDENT_5Wu
                                                     | MASK_VERB_INDEPENDENT_5W_;
  public static final int MASK_VERB_DEPENDENT_1      = 0x00080000; // 非自立・一段
  public static final int MASK_VERB_DEPENDENT_5Ki    = 0x00100000; // 非自立・五段・カ行イ音便
  public static final int MASK_VERB_DEPENDENT_5S     = 0x00200000; // 非自立・五段・サ行
  public static final int MASK_VERB_DEPENDENT_5M     = 0x00400000; // 非自立・五段・マ行
  public static final int MASK_VERB_DEPENDENT_5R     = 0x00800000; // 非自立・五段・ラ行
  public static final int MASK_VERB_DEPENDENT_5Rx    = 0x01000000; // 非自立・五段・ラ行特殊
  public static final int MASK_VERB_DEPENDENT_5W_    = 0x02000000; // 非自立・五段・ワ行促音便
  public static final int MASK_VERB_DEPENDENT        = MASK_VERB_DEPENDENT_1
                                                     | MASK_VERB_DEPENDENT_5Ki
                                                     | MASK_VERB_DEPENDENT_5S
                                                     | MASK_VERB_DEPENDENT_5M
                                                     | MASK_VERB_DEPENDENT_5R
                                                     | MASK_VERB_DEPENDENT_5Rx
                                                     | MASK_VERB_DEPENDENT_5W_;
  public static final int MASK_VERB                  = MASK_VERB_INDEPENDENT | MASK_VERB_DEPENDENT;

  // 形容詞
  public static final int MASK_ADJECTIVE_AUO         = 0x04000000; // 形容詞・アウオ段
  public static final int MASK_ADJECTIVE_I           = 0x08000000; // 形容詞・イ段
  public static final int MASK_ADJECTIVE             = MASK_ADJECTIVE_AUO | MASK_ADJECTIVE_I;

  // 副詞
  public static final int MASK_ADVERB_NONE           = 0x10000000; // 副詞・一般
  public static final int MASK_ADVERB_PARTICLE       = 0x20000000; // 副詞・助詞類接続
  public static final int MASK_ADVERB                = MASK_ADVERB_NONE | MASK_ADVERB_PARTICLE;

  // 連体詞
  public static final int MASK_ADNOMINAL             = 0x40000000;

  // ユーザー定義
  public static final int MASK_USER                  = 0x80000000;

  // 全て
  public static final int MASK_ALL                   = MASK_NOUM
                                                     | MASK_VERB
                                                     | MASK_ADJECTIVE
                                                     | MASK_ADVERB
                                                     | MASK_ADNOMINAL
                                                     | MASK_USER;

  private final Word[] words;
  private final Trie   head;

  private Dictionary(final Word[] words) {
    this.words = words;
    Arrays.sort(words);
    this.head = new Trie.Builder().build(words);
  }

  public static final class Builder {
    public Dictionary build() {
      final List<Word> words = new ArrayList<Word>();

      for (int i = 0, size = Dictionary.NAMES.length; i < size; ++i) {
        final URL dictionary = Dictionary.class.getResource("dic/" + Dictionary.NAMES[i] + Dictionary.EXTENSION);

        final Map<String, String> map = new HashMap<String, String>();
        try {
          final BufferedReader in = new BufferedReader(new InputStreamReader(dictionary.openStream()));
          try {
            String line;
            for (int ln = 1; (line = in.readLine()) != null; ++ln) {
              line = line.trim();
              if (line.isEmpty()) {
                continue;
              }
              final String[] entry = line.split("\\s");
              if (entry.length != 2) {
                System.err.printf("%s(%d): %s\n", dictionary.getFile(), ln, line);
                continue;
              }
              final String kana  = entry[0];
              final String kanji = entry[1];
              {
                final String value = map.get(kana);
                if (value == null) {
                  map.put(kana, kanji);
                } else {
                  map.put(kana, value + "、" + kanji);
                }
              }
            }
          } catch (final IOException e) {
          } finally {
            in.close();
          }
        } catch (final FileNotFoundException e) {
          System.err.printf("辞書ファイルを開けませんでした: %s\n", dictionary.getFile());
        } catch (final IOException e) {
          // in.close() で例外が生じても無視する。
        } finally {
          final int feature = 1 << i;
          for (final Map.Entry<String, String> e : map.entrySet()) {
            words.add(new Word(feature, e.getKey(), e.getValue()));
          }
        }
      }

      return new Dictionary(words.toArray(new Word[words.size()]));
    }
  }

  public Word[] match(final Mora[] query) {
    final Word[] words = head.match(query);
    Arrays.sort(words, new Comparator<Word>() {
      @Override
      public int compare(final Word w1, final Word w2) {
        return w1.distance(query) - w2.distance(query);
      }
    });
    return words;
  }

  public Word[] matchPrefix(final Mora[] query) {
    final Word[] words = head.matchHead(query);
    Arrays.sort(words, new Comparator<Word>() {
      @Override
      public int compare(final Word w1, final Word w2) {
        return w1.distanceHead(query) - w2.distanceHead(query);
      }
    });
    return words;
  }

  public Word[] matchPostfix(final Mora[] query) {
    final Word[] words = head.matchTail(query);
    Arrays.sort(words, new Comparator<Word>() {
      @Override
      public int compare(final Word w1, final Word w2) {
        return w1.distanceTail(query) - w2.distanceTail(query);
      }
    });
    return words;
  }

  public Word[] matchInternal(final Mora[] query) {
    final List<Word> list = new ArrayList<Word>();
    for (final Word word : words) {
      if (word.matchInternal(query)) {
        list.add(word);
      }
    }
    return list.toArray(new Word[list.size()]);
  }
}

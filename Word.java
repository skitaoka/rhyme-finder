import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

final class Word implements Comparable<Word> {

  private final int    feature;
  private final String kana;
  private final String words;
  private final Mora[] mora;

  public Word(final int feature, final String kana, final String words) {
    this.feature = feature;
    this.kana = kana;
    {
      final Set<String> set = new HashSet<String>();
      for (final String word : words.split("、")) {
        set.add(word);
      }

      final String[] array = set.toArray(new String[set.size()]);
      Arrays.sort(array);

      final StringBuilder sb = new StringBuilder(array[0]);
      for (int i = 1, size = array.length; i < size; ++i) {
        sb.append('、');
        sb.append(array[i]);
      }
      this.words = sb.toString();
    }
    this.mora = Word.toMora(kana);
  }

  public Mora[] getMora() {
    return mora;
  }

  public String toHTML(final int feature) {
    if ((this.feature & feature) != 0) {
      return "<dt style=\'background-color:yellow;\'><dfn>" + kana + "</dfn></dt><dd>" + words + "</dd>";
    } else {
      return "<dt><dfn>" + kana + "</dfn></dt><dd>" + words + "</dd>";
    }
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    for (int i = 0, size = mora.length; i < size; ++i) {
      builder.append(' ');
      builder.append(mora[i].toString());
    }
    return kana + '；' + words + '：' + builder.substring(1);
  }

  @Override
  public int compareTo(final Word word) {
    return kana.compareTo(word.kana);
  }

  public int distance(final Mora[] query) {
    if (query.length != mora.length) {
      return Integer.MAX_VALUE;
    }
    int distance = 0;
    for (int i = 0, size = query.length; i < size; ++i) {
      final int distance_mora = query[i].distance(mora[i]);
      if (distance_mora < 0) {
        return Integer.MAX_VALUE;
      }
      distance += distance_mora;
    }
    return distance;
  }

  public int distanceHead(final Mora[] query) {
    if (query.length > mora.length) {
      return Integer.MAX_VALUE;
    }
    int distance = 0;
    for (int i = 0, size = query.length; i < size; ++i) {
      final int distance_mora = query[i].distance(mora[i]);
      if (distance_mora < 0) {
        return Integer.MAX_VALUE;
      }
      distance += distance_mora;
    }
    return distance;
  }

  public int distanceTail(final Mora[] query) {
    if (query.length > mora.length) {
      return Integer.MAX_VALUE;
    }
    int distance = 0;
    for (int i = 1, size = query.length, offset = mora.length; i <= size; ++i) {
      final int distance_mora = query[size-i].distance(mora[offset-i]);
      if (distance_mora < 0) {
        return Integer.MAX_VALUE;
      }
      distance += distance_mora;
    }
    return distance;
  }

/*
int[] distance = new int[mora.length * query.length];
for (int i = 0; i < query.length; ++i) {
  final Mora a = query[i];
  final Mora.Vowel v = a.getVowel();
  for (int j = 0; j < mora.length; ++j) {
    final Mora b = mora[j];
    final Mora.Vowel w = b.getVowel();
    if (v.equals(w) || Moera.Vowel.any.equals(v) || Mora.Vowel.any.equals(w)) {
      int distance_min;
      if (i > 0) {
        distance_min = Integer.MAX_VALUE;
        for (int k = 0; k < j; ++k) {
          final int d = distance[(i-1) * mora.length + k];
          if (distance_min > d) {
            distance_min = d;
          }
        }
      } else {
        distance_min = 0;
      }
      distance[i * mora.length + j] = distance_min + a.distance(b);
    } else {
      distance[i * mora.length + j] = Integer.MAX_VALUE;
  }
}
*/

  public boolean contains(final Mora[] query) {
    for (final Mora q : query) {
      for (final Mora m : mora) {
        if (q.equals(m)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean match(final Mora[] query) {
    if (query.length != mora.length) {
      return false;
    }
    for (int i = 0, size = query.length; i < size; ++i) {
      if (!query[i].equals(mora[i])) {
        return false;
      }
    }
    return true;
  }

  public boolean matchPrefix(final Mora[] query) {
    if (query.length >= mora.length) {
      return false;
    }
    for (int i = 0, size = query.length; i < size; ++i) {
      if (!query[i].equals(mora[i])) {
        return false;
      }
    }
    return true;
  }

  public boolean matchPostfix(final Mora[] query) {
    if (query.length >= mora.length) {
      return false;
    }
    for (int i = 1, size = query.length, offset = mora.length; i <= size; ++i) {
      if (!query[size-i].equals(mora[offset-i])) {
        return false;
      }
    }
    return true;
  }

  public boolean matchInternal(final Mora[] query) {
    if (query.length >= mora.length) {
      return false;
    }
    if (matchPostfix(query)) {
      return false;
    }

    int i = 0;
    for (final Mora q : query) {
      for (;;) {
        if (i < mora.length) {
          if (q.equals(mora[i++])) {
            break;
          }
        } else {
          return false;
        }
      }
    }
    if (i == query.length) {
      return false;
    }

    return true;
  }

  public static Mora[] toMora(final String kana) {
    final List<Mora> list = new ArrayList<Mora>();
    for (int i = 0, length = kana.length(); i < length; ++i) {
           if (kana.startsWith("きゃ", i) || kana.startsWith("キャ", i)) { list.add(new Mora(Mora.Consonant._k_j , Mora.Vowel._a)); }// k_j  a
      else if (kana.startsWith("きゅ", i) || kana.startsWith("キュ", i)) { list.add(new Mora(Mora.Consonant._k_j , Mora.Vowel._M)); }// k_j  M
      else if (kana.startsWith("きぇ", i) || kana.startsWith("キェ", i)) { list.add(new Mora(Mora.Consonant._k_j , Mora.Vowel._e)); }// k_j  e
      else if (kana.startsWith("きょ", i) || kana.startsWith("キョ", i)) { list.add(new Mora(Mora.Consonant._k_j , Mora.Vowel._o)); }// k_j  o
      else if (kana.startsWith("ぎゃ", i) || kana.startsWith("ギャ", i)) { list.add(new Mora(Mora.Consonant._g_j , Mora.Vowel._a)); }// g_j  a
      else if (kana.startsWith("ぎゅ", i) || kana.startsWith("ギュ", i)) { list.add(new Mora(Mora.Consonant._g_j , Mora.Vowel._M)); }// g_j  M
      else if (kana.startsWith("ぎぇ", i) || kana.startsWith("ギェ", i)) { list.add(new Mora(Mora.Consonant._g_j , Mora.Vowel._e)); }// g_j  e
      else if (kana.startsWith("ぎょ", i) || kana.startsWith("ギョ", i)) { list.add(new Mora(Mora.Consonant._g_j , Mora.Vowel._o)); }// g_j  o
      else if (kana.startsWith("しゃ", i) || kana.startsWith("シャ", i)) { list.add(new Mora(Mora.Consonant._S   , Mora.Vowel._a)); }// S    a
      else if (kana.startsWith("しゅ", i) || kana.startsWith("シュ", i)) { list.add(new Mora(Mora.Consonant._S   , Mora.Vowel._M)); }// S    M
      else if (kana.startsWith("しぇ", i) || kana.startsWith("シェ", i)) { list.add(new Mora(Mora.Consonant._S   , Mora.Vowel._e)); }// S    e
      else if (kana.startsWith("しょ", i) || kana.startsWith("ショ", i)) { list.add(new Mora(Mora.Consonant._S   , Mora.Vowel._o)); }// S    o
      else if (kana.startsWith("じゃ", i) || kana.startsWith("ジャ", i)) { list.add(new Mora(Mora.Consonant._dZ  , Mora.Vowel._a)); }// dZ   a
      else if (kana.startsWith("じゅ", i) || kana.startsWith("ジュ", i)) { list.add(new Mora(Mora.Consonant._dZ  , Mora.Vowel._M)); }// dZ   M
      else if (kana.startsWith("じぇ", i) || kana.startsWith("ジェ", i)) { list.add(new Mora(Mora.Consonant._dZ  , Mora.Vowel._e)); }// dZ   e
      else if (kana.startsWith("じょ", i) || kana.startsWith("ジョ", i)) { list.add(new Mora(Mora.Consonant._dZ  , Mora.Vowel._o)); }// dZ   o
      else if (kana.startsWith("ぢゃ", i) || kana.startsWith("ヂャ", i)) { list.add(new Mora(Mora.Consonant._dZ  , Mora.Vowel._a)); }// dZ   a
      else if (kana.startsWith("ぢゅ", i) || kana.startsWith("ヂュ", i)) { list.add(new Mora(Mora.Consonant._dZ  , Mora.Vowel._M)); }// dZ   M
      else if (kana.startsWith("ぢぇ", i) || kana.startsWith("ヂェ", i)) { list.add(new Mora(Mora.Consonant._dZ  , Mora.Vowel._e)); }// dZ   e
      else if (kana.startsWith("ぢょ", i) || kana.startsWith("ヂョ", i)) { list.add(new Mora(Mora.Consonant._dZ  , Mora.Vowel._o)); }// dZ   o
      else if (kana.startsWith("つぁ", i) || kana.startsWith("ツァ", i)) { list.add(new Mora(Mora.Consonant._ts  , Mora.Vowel._a)); }// ts   a
      else if (kana.startsWith("つぃ", i) || kana.startsWith("ツィ", i)) { list.add(new Mora(Mora.Consonant._ts  , Mora.Vowel._M)); }// ts   i
      else if (kana.startsWith("つぇ", i) || kana.startsWith("ツェ", i)) { list.add(new Mora(Mora.Consonant._ts  , Mora.Vowel._e)); }// ts   e
      else if (kana.startsWith("つぉ", i) || kana.startsWith("ツォ", i)) { list.add(new Mora(Mora.Consonant._ts  , Mora.Vowel._o)); }// ts   o
      else if (kana.startsWith("ちゃ", i) || kana.startsWith("チャ", i)) { list.add(new Mora(Mora.Consonant._tS  , Mora.Vowel._a)); }// tS   a
      else if (kana.startsWith("ちゅ", i) || kana.startsWith("チュ", i)) { list.add(new Mora(Mora.Consonant._tS  , Mora.Vowel._M)); }// tS   M
      else if (kana.startsWith("ちぇ", i) || kana.startsWith("チェ", i)) { list.add(new Mora(Mora.Consonant._tS  , Mora.Vowel._e)); }// tS   e
      else if (kana.startsWith("ちょ", i) || kana.startsWith("チョ", i)) { list.add(new Mora(Mora.Consonant._tS  , Mora.Vowel._o)); }// tS   o
      else if (kana.startsWith("にゃ", i) || kana.startsWith("ニャ", i)) { list.add(new Mora(Mora.Consonant._J   , Mora.Vowel._a)); }// J    a
      else if (kana.startsWith("にゅ", i) || kana.startsWith("ニュ", i)) { list.add(new Mora(Mora.Consonant._J   , Mora.Vowel._M)); }// J    M
      else if (kana.startsWith("にぇ", i) || kana.startsWith("ニェ", i)) { list.add(new Mora(Mora.Consonant._J   , Mora.Vowel._e)); }// J    e
      else if (kana.startsWith("にょ", i) || kana.startsWith("ニョ", i)) { list.add(new Mora(Mora.Consonant._J   , Mora.Vowel._o)); }// J    o
      else if (kana.startsWith("ひゃ", i) || kana.startsWith("ヒャ", i)) { list.add(new Mora(Mora.Consonant._C   , Mora.Vowel._a)); }// C    a
      else if (kana.startsWith("ひゅ", i) || kana.startsWith("ヒュ", i)) { list.add(new Mora(Mora.Consonant._C   , Mora.Vowel._M)); }// C    M
      else if (kana.startsWith("ひぇ", i) || kana.startsWith("ヒェ", i)) { list.add(new Mora(Mora.Consonant._C   , Mora.Vowel._e)); }// C    e
      else if (kana.startsWith("ひょ", i) || kana.startsWith("ヒョ", i)) { list.add(new Mora(Mora.Consonant._C   , Mora.Vowel._o)); }// C    o
      else if (kana.startsWith("びゃ", i) || kana.startsWith("ビャ", i)) { list.add(new Mora(Mora.Consonant._b_j , Mora.Vowel._a)); }// b_j  a
      else if (kana.startsWith("びゅ", i) || kana.startsWith("ビュ", i)) { list.add(new Mora(Mora.Consonant._b_j , Mora.Vowel._M)); }// b_j  M
      else if (kana.startsWith("びぇ", i) || kana.startsWith("ビェ", i)) { list.add(new Mora(Mora.Consonant._b_j , Mora.Vowel._e)); }// b_j  e
      else if (kana.startsWith("びょ", i) || kana.startsWith("ビョ", i)) { list.add(new Mora(Mora.Consonant._b_j , Mora.Vowel._o)); }// b_j  o
      else if (kana.startsWith("ぴゃ", i) || kana.startsWith("ピャ", i)) { list.add(new Mora(Mora.Consonant._p_j , Mora.Vowel._a)); }// p_j  a
      else if (kana.startsWith("ぴゅ", i) || kana.startsWith("ピュ", i)) { list.add(new Mora(Mora.Consonant._p_j , Mora.Vowel._M)); }// P_j  M
      else if (kana.startsWith("ぴぇ", i) || kana.startsWith("ピェ", i)) { list.add(new Mora(Mora.Consonant._p_j , Mora.Vowel._e)); }// p_j  e
      else if (kana.startsWith("ぴょ", i) || kana.startsWith("ピョ", i)) { list.add(new Mora(Mora.Consonant._p_j , Mora.Vowel._o)); }// p_j  o
      else if (kana.startsWith("みゃ", i) || kana.startsWith("ミャ", i)) { list.add(new Mora(Mora.Consonant._m_j , Mora.Vowel._a)); }// m_j  a
      else if (kana.startsWith("みゅ", i) || kana.startsWith("ミュ", i)) { list.add(new Mora(Mora.Consonant._m_j , Mora.Vowel._M)); }// m_j  M
      else if (kana.startsWith("みぇ", i) || kana.startsWith("ミェ", i)) { list.add(new Mora(Mora.Consonant._m_j , Mora.Vowel._e)); }// m_j  e
      else if (kana.startsWith("みょ", i) || kana.startsWith("ミョ", i)) { list.add(new Mora(Mora.Consonant._m_j , Mora.Vowel._o)); }// m_j  o
      else if (kana.startsWith("りゃ", i) || kana.startsWith("リャ", i)) { list.add(new Mora(Mora.Consonant._4_j , Mora.Vowel._a)); }// 4_j  a
      else if (kana.startsWith("りゅ", i) || kana.startsWith("リュ", i)) { list.add(new Mora(Mora.Consonant._4_j , Mora.Vowel._M)); }// 4_j  M
      else if (kana.startsWith("りぇ", i) || kana.startsWith("リェ", i)) { list.add(new Mora(Mora.Consonant._4_j , Mora.Vowel._e)); }// 4_j  e
      else if (kana.startsWith("りょ", i) || kana.startsWith("リョ", i)) { list.add(new Mora(Mora.Consonant._4_j , Mora.Vowel._o)); }// 4_j  o
      else if (kana.startsWith("ふぁ", i) || kana.startsWith("ファ", i)) { list.add(new Mora(Mora.Consonant._pb  , Mora.Vowel._a)); }// p\   a
      else if (kana.startsWith("ふぃ", i) || kana.startsWith("フィ", i)) { list.add(new Mora(Mora.Consonant._pb_j, Mora.Vowel._M)); }// p\_j i
      else if (kana.startsWith("ふゅ", i) || kana.startsWith("フュ", i)) { list.add(new Mora(Mora.Consonant._pb_j, Mora.Vowel._e)); }// p\_j e
      else if (kana.startsWith("ふぇ", i) || kana.startsWith("フェ", i)) { list.add(new Mora(Mora.Consonant._pb  , Mora.Vowel._e)); }// p\   e
      else if (kana.startsWith("ふぉ", i) || kana.startsWith("フォ", i)) { list.add(new Mora(Mora.Consonant._pb  , Mora.Vowel._o)); }// p\   o
      else if (kana.startsWith("すぃ", i) || kana.startsWith("スィ", i)) { list.add(new Mora(Mora.Consonant._s   , Mora.Vowel._i)); }// s    i
      else if (kana.startsWith("ずぃ", i) || kana.startsWith("ズィ", i)) { list.add(new Mora(Mora.Consonant._dz  , Mora.Vowel._i)); }// dz   i
      else if (kana.startsWith("てぃ", i) || kana.startsWith("ティ", i)) { list.add(new Mora(Mora.Consonant._t_j , Mora.Vowel._i)); }// t_j  i
      else if (kana.startsWith("てゅ", i) || kana.startsWith("テュ", i)) { list.add(new Mora(Mora.Consonant._t_j , Mora.Vowel._M)); }// t_j  M
      else if (kana.startsWith("とぅ", i) || kana.startsWith("トゥ", i)) { list.add(new Mora(Mora.Consonant._t   , Mora.Vowel._M)); }// t    M
      else if (kana.startsWith("でぃ", i) || kana.startsWith("ディ", i)) { list.add(new Mora(Mora.Consonant._d_j , Mora.Vowel._i)); }// d_j  i
      else if (kana.startsWith("でゅ", i) || kana.startsWith("デュ", i)) { list.add(new Mora(Mora.Consonant._d_j , Mora.Vowel._M)); }// d_j  M
      else if (kana.startsWith("どぅ", i) || kana.startsWith("ドゥ", i)) { list.add(new Mora(Mora.Consonant._d   , Mora.Vowel._M)); }// d    M
      else if (kana.startsWith("うぁ", i) || kana.startsWith("ウァ", i)) { list.add(new Mora(Mora.Consonant._w   , Mora.Vowel._a)); }// w    a
      else if (kana.startsWith("うぃ", i) || kana.startsWith("ウィ", i)) { list.add(new Mora(Mora.Consonant._w   , Mora.Vowel._i)); }// w    i
      else if (kana.startsWith("うぇ", i) || kana.startsWith("ウェ", i)) { list.add(new Mora(Mora.Consonant._w   , Mora.Vowel._e)); }// w    e
      else if (kana.startsWith("うぉ", i) || kana.startsWith("ウォ", i)) { list.add(new Mora(Mora.Consonant._w   , Mora.Vowel._o)); }// w    o
      else if (kana.startsWith("ヴぁ", i) || kana.startsWith("ヴァ", i)) { list.add(new Mora(Mora.Consonant._v   , Mora.Vowel._a)); }// v    a
      else if (kana.startsWith("ヴぃ", i) || kana.startsWith("ヴィ", i)) { list.add(new Mora(Mora.Consonant._v   , Mora.Vowel._i)); }// v    i
      else if (kana.startsWith("ヴぇ", i) || kana.startsWith("ヴェ", i)) { list.add(new Mora(Mora.Consonant._v   , Mora.Vowel._e)); }// v    e
      else if (kana.startsWith("ヴぉ", i) || kana.startsWith("ヴォ", i)) { list.add(new Mora(Mora.Consonant._v   , Mora.Vowel._o)); }// v    o
      else if (kana.startsWith("くぁ", i) || kana.startsWith("クァ", i)) { list.add(new Mora(Mora.Consonant._k_w , Mora.Vowel._a)); }// k_w  a
      else if (kana.startsWith("くぃ", i) || kana.startsWith("クィ", i)) { list.add(new Mora(Mora.Consonant._k_w , Mora.Vowel._i)); }// k_w  i
      else if (kana.startsWith("くぇ", i) || kana.startsWith("クェ", i)) { list.add(new Mora(Mora.Consonant._k_w , Mora.Vowel._e)); }// k_w  e
      else if (kana.startsWith("くぉ", i) || kana.startsWith("クォ", i)) { list.add(new Mora(Mora.Consonant._k_w , Mora.Vowel._o)); }// k_w  o
      else if (kana.startsWith("ぐぁ", i) || kana.startsWith("グァ", i)) { list.add(new Mora(Mora.Consonant._g_w , Mora.Vowel._a)); }// g_w  a
      else if (kana.startsWith("ぐぃ", i) || kana.startsWith("グィ", i)) { list.add(new Mora(Mora.Consonant._g_w , Mora.Vowel._i)); }// g_w  i
      else if (kana.startsWith("ぐぇ", i) || kana.startsWith("グェ", i)) { list.add(new Mora(Mora.Consonant._g_w , Mora.Vowel._e)); }// g_w  e
      else if (kana.startsWith("ぐぉ", i) || kana.startsWith("グォ", i)) { list.add(new Mora(Mora.Consonant._g_w , Mora.Vowel._o)); }// g_w  o
      else if (kana.startsWith("いぇ", i) || kana.startsWith("イェ", i)) { list.add(new Mora(Mora.Consonant._j   , Mora.Vowel._e)); }// j    e
      else {
        switch (kana.charAt(i)) {
        case 'あ': case 'ア': { list.add(new Mora(Mora.Consonant.none, Mora.Vowel._a  )); } break;                   //      a
        case 'い': case 'イ': { list.add(new Mora(Mora.Consonant.none, Mora.Vowel._i  )); } break;                   //      i
        case 'う': case 'ウ': { list.add(new Mora(Mora.Consonant.none, Mora.Vowel._M  )); } break;                   //      M
        case 'え': case 'エ': { list.add(new Mora(Mora.Consonant.none, Mora.Vowel._e  )); } break;                   //      e
        case 'お': case 'オ': { list.add(new Mora(Mora.Consonant.none, Mora.Vowel._o  )); } break;                   //      o
        case 'か': case 'カ': { list.add(new Mora(Mora.Consonant._k  , Mora.Vowel._a  )); } break;                   // k    a
        case 'き': case 'キ': { list.add(new Mora(Mora.Consonant._k_j, Mora.Vowel._i  )); } break;                   // k_j  i
        case 'く': case 'ク': { list.add(new Mora(Mora.Consonant._k  , Mora.Vowel._M  )); } break;                   // k    M
        case 'け': case 'ケ': { list.add(new Mora(Mora.Consonant._k  , Mora.Vowel._e  )); } break;                   // k    e
        case 'こ': case 'コ': { list.add(new Mora(Mora.Consonant._k  , Mora.Vowel._o  )); } break;                   // k    o
        case 'が': case 'ガ': { list.add(new Mora(Mora.Consonant._g  , Mora.Vowel._a  )); } break;                   // g    a
        case 'ぎ': case 'ギ': { list.add(new Mora(Mora.Consonant._g_j, Mora.Vowel._i  )); } break;                   // g_j  i
        case 'ぐ': case 'グ': { list.add(new Mora(Mora.Consonant._g  , Mora.Vowel._M  )); } break;                   // g    M
        case 'げ': case 'ゲ': { list.add(new Mora(Mora.Consonant._g  , Mora.Vowel._e  )); } break;                   // g    e
        case 'ご': case 'ゴ': { list.add(new Mora(Mora.Consonant._g  , Mora.Vowel._o  )); } break;                   // g    o
        case 'さ': case 'サ': { list.add(new Mora(Mora.Consonant._s  , Mora.Vowel._a  )); } break;                   // s    a
        case 'し': case 'シ': { list.add(new Mora(Mora.Consonant._S  , Mora.Vowel._i  )); } break;                   // S    i
        case 'す': case 'ス': { list.add(new Mora(Mora.Consonant._s  , Mora.Vowel._M  )); } break;                   // s    M
        case 'せ': case 'セ': { list.add(new Mora(Mora.Consonant._s  , Mora.Vowel._e  )); } break;                   // s    e
        case 'そ': case 'ソ': { list.add(new Mora(Mora.Consonant._s  , Mora.Vowel._o  )); } break;                   // s    o
        case 'ざ': case 'ザ': { list.add(new Mora(Mora.Consonant._dz , Mora.Vowel._a  )); } break;                   // dz   a
        case 'じ': case 'ジ': { list.add(new Mora(Mora.Consonant._dZ , Mora.Vowel._i  )); } break;                   // dZ   i
        case 'ず': case 'ズ': { list.add(new Mora(Mora.Consonant._dz , Mora.Vowel._M  )); } break;                   // dz   M
        case 'ぜ': case 'ゼ': { list.add(new Mora(Mora.Consonant._dz , Mora.Vowel._e  )); } break;                   // dz   e
        case 'ぞ': case 'ゾ': { list.add(new Mora(Mora.Consonant._dz , Mora.Vowel._o  )); } break;                   // dz   o
        case 'た': case 'タ': { list.add(new Mora(Mora.Consonant._t  , Mora.Vowel._a  )); } break;                   // t    a
        case 'ち': case 'チ': { list.add(new Mora(Mora.Consonant._tS , Mora.Vowel._i  )); } break;                   // tS   i
        case 'つ': case 'ツ': { list.add(new Mora(Mora.Consonant._ts , Mora.Vowel._M  )); } break;                   // ts   M
        case 'て': case 'テ': { list.add(new Mora(Mora.Consonant._t  , Mora.Vowel._e  )); } break;                   // t    e
        case 'と': case 'ト': { list.add(new Mora(Mora.Consonant._t  , Mora.Vowel._o  )); } break;                   // t    o
        case 'だ': case 'ダ': { list.add(new Mora(Mora.Consonant._d  , Mora.Vowel._a  )); } break;                   // d    a
        case 'ぢ': case 'ヂ': { list.add(new Mora(Mora.Consonant._dZ , Mora.Vowel._i  )); } break;                   // dZ   i
        case 'づ': case 'ヅ': { list.add(new Mora(Mora.Consonant._dz , Mora.Vowel._M  )); } break;                   // dz   M
        case 'で': case 'デ': { list.add(new Mora(Mora.Consonant._d  , Mora.Vowel._e  )); } break;                   // d    e
        case 'ど': case 'ド': { list.add(new Mora(Mora.Consonant._d  , Mora.Vowel._o  )); } break;                   // d    o
        case 'な': case 'ナ': { list.add(new Mora(Mora.Consonant._n  , Mora.Vowel._a  )); } break;                   // n    a
        case 'に': case 'ニ': { list.add(new Mora(Mora.Consonant._J  , Mora.Vowel._i  )); } break;                   // J    i
        case 'ぬ': case 'ヌ': { list.add(new Mora(Mora.Consonant._n  , Mora.Vowel._M  )); } break;                   // n    M
        case 'ね': case 'ネ': { list.add(new Mora(Mora.Consonant._n  , Mora.Vowel._e  )); } break;                   // n    e
        case 'の': case 'ノ': { list.add(new Mora(Mora.Consonant._n  , Mora.Vowel._o  )); } break;                   // n    o
        case 'は': case 'ハ': { list.add(new Mora(Mora.Consonant._h  , Mora.Vowel._a  )); } break;                   // h    a
        case 'ひ': case 'ヒ': { list.add(new Mora(Mora.Consonant._C  , Mora.Vowel._i  )); } break;                   // C    i
        case 'ふ': case 'フ': { list.add(new Mora(Mora.Consonant._pb , Mora.Vowel._M  )); } break;                   // p\   M
        case 'へ': case 'ヘ': { list.add(new Mora(Mora.Consonant._h  , Mora.Vowel._e  )); } break;                   // h    e
        case 'ほ': case 'ホ': { list.add(new Mora(Mora.Consonant._h  , Mora.Vowel._o  )); } break;                   // h    o
        case 'ば': case 'バ': { list.add(new Mora(Mora.Consonant._b  , Mora.Vowel._a  )); } break;                   // b    a
        case 'び': case 'ビ': { list.add(new Mora(Mora.Consonant._b_j, Mora.Vowel._i  )); } break;                   // b_j  i
        case 'ぶ': case 'ブ': { list.add(new Mora(Mora.Consonant._b  , Mora.Vowel._M  )); } break;                   // b    M
        case 'べ': case 'ベ': { list.add(new Mora(Mora.Consonant._b  , Mora.Vowel._e  )); } break;                   // b    e
        case 'ぼ': case 'ボ': { list.add(new Mora(Mora.Consonant._b  , Mora.Vowel._o  )); } break;                   // b    o
        case 'ぱ': case 'パ': { list.add(new Mora(Mora.Consonant._p  , Mora.Vowel._a  )); } break;                   // p    a
        case 'ぴ': case 'ピ': { list.add(new Mora(Mora.Consonant._p_j, Mora.Vowel._i  )); } break;                   // p_j  i
        case 'ぷ': case 'プ': { list.add(new Mora(Mora.Consonant._p  , Mora.Vowel._M  )); } break;                   // p    M
        case 'ぺ': case 'ペ': { list.add(new Mora(Mora.Consonant._p  , Mora.Vowel._e  )); } break;                   // p    e
        case 'ぽ': case 'ポ': { list.add(new Mora(Mora.Consonant._p  , Mora.Vowel._o  )); } break;                   // p    o
        case 'ま': case 'マ': { list.add(new Mora(Mora.Consonant._m  , Mora.Vowel._a  )); } break;                   // m    a
        case 'み': case 'ミ': { list.add(new Mora(Mora.Consonant._m_j, Mora.Vowel._i  )); } break;                   // m_j  i
        case 'む': case 'ム': { list.add(new Mora(Mora.Consonant._m  , Mora.Vowel._M  )); } break;                   // m    M
        case 'め': case 'メ': { list.add(new Mora(Mora.Consonant._m  , Mora.Vowel._e  )); } break;                   // m    e
        case 'も': case 'モ': { list.add(new Mora(Mora.Consonant._m  , Mora.Vowel._o  )); } break;                   // m    o
        case 'や': case 'ヤ': { list.add(new Mora(Mora.Consonant._j  , Mora.Vowel._a  )); } break;                   // j    a
        case 'ゆ': case 'ユ': { list.add(new Mora(Mora.Consonant._j  , Mora.Vowel._M  )); } break;                   // j    M
        case 'よ': case 'ヨ': { list.add(new Mora(Mora.Consonant._j  , Mora.Vowel._o  )); } break;                   // j    o
        case 'ら': case 'ラ': { list.add(new Mora(Mora.Consonant._4  , Mora.Vowel._a  )); } break;                   // 4    a
        case 'り': case 'リ': { list.add(new Mora(Mora.Consonant._4_j, Mora.Vowel._i  )); } break;                   // 4_j  i
        case 'る': case 'ル': { list.add(new Mora(Mora.Consonant._4  , Mora.Vowel._M  )); } break;                   // 4    M
        case 'れ': case 'レ': { list.add(new Mora(Mora.Consonant._4  , Mora.Vowel._e  )); } break;                   // 4    e
        case 'ろ': case 'ロ': { list.add(new Mora(Mora.Consonant._4  , Mora.Vowel._o  )); } break;                   // 4    o
        case 'わ': case 'ワ': { list.add(new Mora(Mora.Consonant._w  , Mora.Vowel._a  )); } break;                   // w    a
        case 'を': case 'ヲ': { list.add(new Mora(Mora.Consonant.none, Mora.Vowel._o  )); } break;                   //      o
        case 'ん': case 'ン': { list.add(new Mora(Mora.Consonant.none, Mora.Vowel.none)); } break;                   // N
        case 'っ': case 'ッ': { list.add(new Mora(Mora.Consonant.none, Mora.Vowel.none)); } break;                   // ^
        case 'ー': if (!list.isEmpty()) { list.add(list.get(list.size()-1).getVowelMora()); } break;                   // -

        case 'a' : { list.add(new Mora(Mora.Consonant.any  , Mora.Vowel._a  )); } break;
        case 'i' : { list.add(new Mora(Mora.Consonant.any  , Mora.Vowel._i  )); } break;
        case 'u' : { list.add(new Mora(Mora.Consonant.any  , Mora.Vowel._M  )); } break;
        case 'e' : { list.add(new Mora(Mora.Consonant.any  , Mora.Vowel._e  )); } break;
        case 'o' : { list.add(new Mora(Mora.Consonant.any  , Mora.Vowel._o  )); } break;
        case '.' : { list.add(new Mora(Mora.Consonant.any  , Mora.Vowel.any )); } break;

        case 'k' : { list.add(new Mora(Mora.Consonant._k   , Mora.Vowel.any )); } break;
        case 'K' : { list.add(new Mora(Mora.Consonant._k_j , Mora.Vowel.any )); } break;
        case 'q' : { list.add(new Mora(Mora.Consonant._k_w , Mora.Vowel.any )); } break;
        case 'g' : { list.add(new Mora(Mora.Consonant._g   , Mora.Vowel.any )); } break;
        case 'G' : { list.add(new Mora(Mora.Consonant._g_j , Mora.Vowel.any )); } break;
        case 'Q' : { list.add(new Mora(Mora.Consonant._g_w , Mora.Vowel.any )); } break;
        case 't' : { list.add(new Mora(Mora.Consonant._t   , Mora.Vowel.any )); } break;
        case 'T' : { list.add(new Mora(Mora.Consonant._t_j , Mora.Vowel.any )); } break;
        case 'd' : { list.add(new Mora(Mora.Consonant._d   , Mora.Vowel.any )); } break;
        case 'D' : { list.add(new Mora(Mora.Consonant._d_j , Mora.Vowel.any )); } break;
        case 'p' : { list.add(new Mora(Mora.Consonant._p   , Mora.Vowel.any )); } break;
        case 'P' : { list.add(new Mora(Mora.Consonant._p_j , Mora.Vowel.any )); } break;
        case 'b' : { list.add(new Mora(Mora.Consonant._b   , Mora.Vowel.any )); } break;
        case 'B' : { list.add(new Mora(Mora.Consonant._b_j , Mora.Vowel.any )); } break;
        case 'm' : { list.add(new Mora(Mora.Consonant._m   , Mora.Vowel.any )); } break;
        case 'M' : { list.add(new Mora(Mora.Consonant._m_j , Mora.Vowel.any )); } break;
        case 'r' : { list.add(new Mora(Mora.Consonant._4   , Mora.Vowel.any )); } break;
        case 'R' : { list.add(new Mora(Mora.Consonant._4_j , Mora.Vowel.any )); } break;
        case 's' : { list.add(new Mora(Mora.Consonant._s   , Mora.Vowel.any )); } break;
        case 'S' : { list.add(new Mora(Mora.Consonant._S   , Mora.Vowel.any )); } break;
        case 'n' : { list.add(new Mora(Mora.Consonant._n   , Mora.Vowel.any )); } break;
        case 'N' : { list.add(new Mora(Mora.Consonant._J   , Mora.Vowel.any )); } break;
        case 'h' : { list.add(new Mora(Mora.Consonant._h   , Mora.Vowel.any )); } break;
        case 'H' : { list.add(new Mora(Mora.Consonant._C   , Mora.Vowel.any )); } break;
        case 'y' : { list.add(new Mora(Mora.Consonant._j   , Mora.Vowel.any )); } break;
        case 'w' : { list.add(new Mora(Mora.Consonant._w   , Mora.Vowel.any )); } break;
        case 'v' : { list.add(new Mora(Mora.Consonant._v   , Mora.Vowel.any )); } break;
        case 'z' : { list.add(new Mora(Mora.Consonant._dz  , Mora.Vowel.any )); } break;
        case 'Z' : { list.add(new Mora(Mora.Consonant._dZ  , Mora.Vowel.any )); } break;
        case 'c' : { list.add(new Mora(Mora.Consonant._ts  , Mora.Vowel.any )); } break;
        case 'C' : { list.add(new Mora(Mora.Consonant._tS  , Mora.Vowel.any )); } break;
        case 'f' : { list.add(new Mora(Mora.Consonant._pb  , Mora.Vowel.any )); } break;
        case 'F' : { list.add(new Mora(Mora.Consonant._pb_j, Mora.Vowel.any )); } break;

        case '\'': if (!list.isEmpty()) { list.add(list.remove(list.size()-1).cloneAccent   ()); } break; // 強勢を付与
        case '^' : if (!list.isEmpty()) { list.add(list.remove(list.size()-1).cloneConsonant()); } break; // 母音を削除
        case '-' : if (!list.isEmpty()) { list.add(list.remove(list.size()-1).cloneVowel    ()); } break; // 子音を削除

        case 'ァ': case 'ィ': case 'ゥ':
        case 'ェ': case 'ォ': case 'ヮ':
        default:
          javax.swing.JOptionPane.showMessageDialog(null,
            kana + " (" + kana.charAt(i) + ")",
            "対応していない発音です",
            javax.swing.JOptionPane.ERROR_MESSAGE);
          return new Mora[0];
        }
        --i;
      }
      ++i;
    }
    return list.toArray(new Mora[list.size()]);
  }
}

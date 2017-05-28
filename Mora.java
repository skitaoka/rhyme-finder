/**
 * cf. http://ja.wikipedia.org/wiki/X-SAMPA
 */
final class Mora
{
  public enum Consonant {
    _p  , _t  , _k  , _k_w,    _s, _h, _ts, _pb  ,    _b  , _g  , _g_w, _d  , _v,    _dz, _w, _4  ,    _m  , _n,
    _p_j, _t_j, _k_j,          _S, _C, _tS, _pb_j,    _b_j, _g_j,       _d_j,        _dZ, _j, _4_j,    _m_j, _J,
    none, count, any,
  }

  public enum Vowel {
    _a, _i, _M, _e, _o,
    none, count, any,
  }

  private final Consonant c;
  private final Vowel     v;
  private final int       a;

  public Mora(final Consonant c, final Vowel v) {
    this(c, v, 0);
  }

  public Mora(final Consonant c, final Vowel v, final int a) {
    this.c = c;
    this.v = v;
    this.a = a;
  }

  @Override
  public boolean equals(final Object object) {
    return (object != null)
      && (object instanceof Mora)
      && this.equals((Mora)object);
  }

  public boolean equals(final Mora mora) {
    return (mora != null)
      && (c.equals(mora.c) || Mora.Consonant.any.equals(c) || Mora.Consonant.any.equals(mora.c))
      && (v.equals(mora.v) || Mora.Vowel    .any.equals(v) || Mora.Vowel    .any.equals(mora.v));
  }

  @Override
  public int hashCode() {
    return c.ordinal() * Vowel.count.ordinal() + v.ordinal();
  }

  @Override
  public String toString() {
    return c.toString() + v.toString();
  }

  public Vowel getVowel() {
    return v;
  }

  public Mora getVowelMora() {
    // 長音を生成するためのものなので、
    // 子音が〈none〉であることに注意。
    return new Mora(Consonant.none, v);
  }

  public Mora cloneConsonant() {
    // 母音の無視を指定するために使用するものなので、
    // 母音が〈any〉であることに注意。
    return new Mora(c, Vowel.any);
  }

  public Mora cloneVowel() {
    // 子音の無視を指定するために使用するものなので、
    // 子音が〈any〉であることに注意。
    return new Mora(Consonant.any, v);
  }

  public Mora cloneAccent() {
    return new Mora(c, v, a+1);
  }

  public int distance(final Mora mora) {
    final int accent = 1 + this.a + mora.a;
    return accent * Mora.distance(c, mora.c);
  }

  private static int distance(final Consonant a, final Consonant b) {
    if (a.equals(b) || Consonant.any.equals(a) || Consonant.any.equals(b)) {
      return 0;
    }

    final boolean order = a.ordinal() <= b.ordinal();
    switch (order ? a : b) {
    case _p  : case _t  : case _k  : case _k_w :          return Mora.distance_from_0(order ? b : a);
    case _s  : case _h  : case _ts : case _pb  :          return Mora.distance_from_1(order ? b : a);
    case _b  : case _g  : case _g_w: case _d   : case _v: return Mora.distance_from_2(order ? b : a);
    case _dz : case _w  : case _4  : case none :          return Mora.distance_from_3(order ? b : a);
    case _m  : case _n  :                                 return Mora.distance_from_4(order ? b : a);
    case _p_j: case _t_j: case _k_j:                      return Mora.distance_from_5(order ? b : a);
    case _S  : case _C  : case _tS : case _pb_j:          return Mora.distance_from_6(order ? b : a);
    case _b_j: case _g_j: case _d_j:                      return Mora.distance_from_7(order ? b : a);
    case _dZ : case _j  : case _4_j:                      return Mora.distance_from_8(order ? b : a);
    case _m_j: case _J  :                                 return Mora.distance_from_9(order ? b : a);
    default:                                              return -1;
    }
  }

  private static int distance_from_0(final Consonant a) {
    switch (a) {
    case _p  : case _t  : case _k  : case _k_w :          return 1;
    case _s  : case _h  : case _ts : case _pb  :          return 2;
    case _b  : case _g  : case _g_w: case _d   : case _v: return 2;
    case _dz : case _w  : case _4  : case none :          return 3;
    case _m  : case _n  :                                 return 3;
    case _p_j: case _t_j: case _k_j:                      return 4;
    case _S  : case _C  : case _tS : case _pb_j:          return 5;
    case _b_j: case _g_j: case _d_j:                      return 5;
    case _dZ : case _j  : case _4_j:                      return 6;
    case _m_j: case _J  :                                 return 6;
    default:                                              return -1;
    }
  }

  private static int distance_from_1(final Consonant a) {
    switch (a) {
    case _p  : case _t  : case _k  : case _k_w :          return 2;
    case _s  : case _h  : case _ts : case _pb  :          return 1;
    case _b  : case _g  : case _g_w: case _d   : case _v: return 3;
    case _dz : case _w  : case _4  : case none :          return 2;
    case _m  : case _n  :                                 return 4;
    case _p_j: case _t_j: case _k_j:                      return 5;
    case _S  : case _C  : case _tS : case _pb_j:          return 4;
    case _b_j: case _g_j: case _d_j:                      return 6;
    case _dZ : case _j  : case _4_j:                      return 5;
    case _m_j: case _J  :                                 return 7;
    default:                                              return -1;
    }
  }

  private static int distance_from_2(final Consonant a) {
    switch (a) {
    case _p  : case _t  : case _k  : case _k_w :          return 2;
    case _s  : case _h  : case _ts : case _pb  :          return 3;
    case _b  : case _g  : case _g_w: case _d   : case _v: return 1;
    case _dz : case _w  : case _4  : case none :          return 2;
    case _m  : case _n  :                                 return 2;
    case _p_j: case _t_j: case _k_j:                      return 5;
    case _S  : case _C  : case _tS : case _pb_j:          return 6;
    case _b_j: case _g_j: case _d_j:                      return 4;
    case _dZ : case _j  : case _4_j:                      return 5;
    case _m_j: case _J  :                                 return 5;
    default:                                              return -1;
    }
  }

  private static int distance_from_3(final Consonant a) {
    switch (a) {
    case _p  : case _t  : case _k  : case _k_w :          return 3;
    case _s  : case _h  : case _ts : case _pb  :          return 2;
    case _b  : case _g  : case _g_w: case _d   : case _v: return 2;
    case _dz : case _w  : case _4  : case none :          return 1;
    case _m  : case _n  :                                 return 3;
    case _p_j: case _t_j: case _k_j:                      return 6;
    case _S  : case _C  : case _tS : case _pb_j:          return 5;
    case _b_j: case _g_j: case _d_j:                      return 5;
    case _dZ : case _j  : case _4_j:                      return 4;
    case _m_j: case _J  :                                 return 6;
    default:                                              return -1;
    }
  }

  private static int distance_from_4(final Consonant a) {
    switch (a) {
    case _p  : case _t  : case _k  : case _k_w :          return 3;
    case _s  : case _h  : case _ts : case _pb  :          return 4;
    case _b  : case _g  : case _g_w: case _d   : case _v: return 2;
    case _dz : case _w  : case _4  : case none :          return 3;
    case _m  : case _n  :                                 return 1;
    case _p_j: case _t_j: case _k_j:                      return 6;
    case _S  : case _C  : case _tS : case _pb_j:          return 7;
    case _b_j: case _g_j: case _d_j:                      return 5;
    case _dZ : case _j  : case _4_j:                      return 6;
    case _m_j: case _J  :                                 return 4;
    default:                                              return -1;
    }
  }

  private static int distance_from_5(final Consonant a) {
    switch (a) {
    case _p  : case _t  : case _k  : case _k_w :          return 4;
    case _s  : case _h  : case _ts : case _pb  :          return 5;
    case _b  : case _g  : case _g_w: case _d   : case _v: return 5;
    case _dz : case _w  : case _4  : case none :          return 6;
    case _m  : case _n  :                                 return 6;
    case _p_j: case _t_j: case _k_j:                      return 1;
    case _S  : case _C  : case _tS : case _pb_j:          return 2;
    case _b_j: case _g_j: case _d_j:                      return 2;
    case _dZ : case _j  : case _4_j:                      return 3;
    case _m_j: case _J  :                                 return 3;
    default:                                              return -1;
    }
  }

  private static int distance_from_6(final Consonant a) {
    switch (a) {
    case _p  : case _t  : case _k  : case _k_w :          return 4;
    case _s  : case _h  : case _ts : case _pb  :          return 5;
    case _b  : case _g  : case _g_w: case _d   : case _v: return 5;
    case _dz : case _w  : case _4  : case none :          return 6;
    case _m  : case _n  :                                 return 6;
    case _p_j: case _t_j: case _k_j:                      return 1;
    case _S  : case _C  : case _tS : case _pb_j:          return 2;
    case _b_j: case _g_j: case _d_j:                      return 2;
    case _dZ : case _j  : case _4_j:                      return 3;
    case _m_j: case _J  :                                 return 3;
    default:                                              return -1;
    }
  }

  private static int distance_from_7(final Consonant a) {
    switch (a) {
    case _p  : case _t  : case _k  : case _k_w :          return 5;
    case _s  : case _h  : case _ts : case _pb  :          return 6;
    case _b  : case _g  : case _g_w: case _d   : case _v: return 4;
    case _dz : case _w  : case _4  : case none :          return 5;
    case _m  : case _n  :                                 return 5;
    case _p_j: case _t_j: case _k_j:                      return 2;
    case _S  : case _C  : case _tS : case _pb_j:          return 3;
    case _b_j: case _g_j: case _d_j:                      return 1;
    case _dZ : case _j  : case _4_j:                      return 2;
    case _m_j: case _J  :                                 return 2;
    default:                                              return -1;
    }
  }

  private static int distance_from_8(final Consonant a) {
    switch (a) {
    case _p  : case _t  : case _k  : case _k_w :          return 6;
    case _s  : case _h  : case _ts : case _pb  :          return 5;
    case _b  : case _g  : case _g_w: case _d   : case _v: return 5;
    case _dz : case _w  : case _4  : case none :          return 4;
    case _m  : case _n  :                                 return 6;
    case _p_j: case _t_j: case _k_j:                      return 3;
    case _S  : case _C  : case _tS : case _pb_j:          return 2;
    case _b_j: case _g_j: case _d_j:                      return 2;
    case _dZ : case _j  : case _4_j:                      return 1;
    case _m_j: case _J  :                                 return 3;
    default:                                              return -1;
    }
  }

  private static int distance_from_9(final Consonant a) {
    switch (a) {
    case _p  : case _t  : case _k  : case _k_w :          return 6;
    case _s  : case _h  : case _ts : case _pb  :          return 4;
    case _b  : case _g  : case _g_w: case _d   : case _v: return 5;
    case _dz : case _w  : case _4  : case none :          return 6;
    case _m  : case _n  :                                 return 4;
    case _p_j: case _t_j: case _k_j:                      return 3;
    case _S  : case _C  : case _tS : case _pb_j:          return 4;
    case _b_j: case _g_j: case _d_j:                      return 2;
    case _dZ : case _j  : case _4_j:                      return 3;
    case _m_j: case _J  :                                 return 1;
    default:                                              return -1;
    }
  }
}

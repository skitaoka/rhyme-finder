import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

final class Trie {
  private final Node rootNext;
  private final Node rootPrev;

  private Trie(final Node rootNext, final Node rootPrev) {
    this.rootNext = rootNext;
    this.rootPrev = rootPrev;
  }

  public Word[] match(final Mora[] query) {
    final List<Word> list = new ArrayList<Word>();
    rootNext.collect(query, list);
    return list.toArray(new Word[list.size()]);
  }

  public Word[] matchHead(final Mora[] query) {
    final List<Word> list = new ArrayList<Word>();
    rootNext.collectHead(query, list);
    return list.toArray(new Word[list.size()]);
  }

  public Word[] matchTail(final Mora[] query) {
    final List<Word> list = new ArrayList<Word>();
    rootPrev.collectTail(query, list);
    return list.toArray(new Word[list.size()]);
  }

  private static final class Node {
    private final Word[] array;
    private final Node[] next;

    public Node(final Word[] array, final Node[] next) {
      this.array = array;
      this.next  = next;
    }

    public Word[] array() {
      return array;
    }

    public void collect(final Mora[] query, final List<Word> list) {
      this.collect(query, 0, list);
    }

    private void collect(final Mora[] query, final int i, final List<Word> list) {
      if (i >= query.length) {
        // マッチしたので回収
        list.addAll(Arrays.asList(array));
        return;
      }

      final Mora.Vowel v = query[i].getVowel();
      if (Mora.Vowel.any.equals(v)) {
        for (final Node node : next) {
          if (node != null) {
            node.collect(query, i+1, list);
          }
        }
      } else {
        final Node node = next[v.ordinal()];
        if (node != null) {
          node.collect(query, i+1, list);
        }
      }
    }

    public void collectHead(final Mora[] query, final List<Word> list) {
      this.collectHead(query, 0, list);
    }

    private void collectHead(final Mora[] query, final int i, final List<Word> list) {
      if (i >= query.length) {
        // マッチしたので回収
        for (final Node node : next) {
          if (node != null) {
            node.collectAll(list);
          }
        }
        return;
      }

      final Mora.Vowel v = query[i].getVowel();
      if (Mora.Vowel.any.equals(v)) {
        for (final Node node : next) {
          if (node != null) {
            node.collectHead(query, i+1, list);
          }
        }
      } else {
        final Node node = next[v.ordinal()];
        if (node != null) {
          node.collectHead(query, i+1, list);
        }
      }
    }

    public void collectTail(final Mora[] query, final List<Word> list) {
      this.collectTail(query, 0, list);
    }

    private void collectTail(final Mora[] query, final int i, final List<Word> list) {
      if (i >= query.length) {
        // マッチしたので回収
        for (final Node node : next) {
          if (node != null) {
            node.collectAll(list);
          }
        }
        return;
      }

      final Mora.Vowel v = query[query.length-1-i].getVowel();
      if (Mora.Vowel.any.equals(v)) {
        for (final Node node : next) {
          if (node != null) {
            node.collectTail(query, i+1, list);
          }
        }
      } else {
        final Node node = next[v.ordinal()];
        if (node != null) {
          node.collectTail(query, i+1, list);
        }
      }
    }

    private void collectAll(final List<Word> list) {
      list.addAll(Arrays.asList(array));
      for (final Node node : next) {
        if (node != null) {
          node.collectAll(list);
        }
      }
    }

    public static final class Builder {
      private final List<Word> list = new ArrayList<Word>();

      private final Builder[] next = new Builder[Mora.Vowel.count.ordinal()];

      public Builder get(final Mora mora) {
        final int ordinal = mora.getVowel().ordinal();
        final Builder node = next[ordinal];
        if (node == null) {
          return next[ordinal] = new Builder();
        } else {
          return node;
        }
      }

      public void add(final Word word) {
        list.add(word);
      }

      public Node build() {
        final Node[] next = new Node[this.next.length];
        for (int i = 0, size = next.length; i < size; ++i) {
          if (this.next[i] != null) {
            next[i] = this.next[i].build();
          }
        }
        return new Node(list.toArray(new Word[list.size()]), next);
      }
    }
  }

  public static final class Builder {
    public Trie build(final Word[] words) {
      final Node.Builder rootNext = new Node.Builder();
      final Node.Builder rootPrev = new Node.Builder();
      for (final Word word : words) {
        {
          Node.Builder node = rootNext;
          for (final Mora mora : word.getMora()) {
            node = node.get(mora);
          }
          node.add(word);
        }
        {
          Node.Builder node = rootPrev;
          final Mora[] mora = word.getMora();
          for (int i = mora.length - 1; i >= 0; --i) {
            node = node.get(mora[i]);
          }
          node.add(word);
        }
      }
      return new Trie(rootNext.build(), rootPrev.build());
    }
  }
}

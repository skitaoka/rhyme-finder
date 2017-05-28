import java.awt.Container;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

//<applet code="RhymeFinder$Applet.class" archive="rhyme.jar" width="100" height="50"></applet>
public final class RhymeFinder extends JFrame {

  private final JTextField queryMora = new JTextField();
  private final JTextField blacklist = new JTextField();

  private final JEditorPane strict   = new JEditorPane("text/html", "");
  private final JEditorPane head     = new JEditorPane("text/html", "");
  private final JEditorPane tail     = new JEditorPane("text/html", "");
  private final JEditorPane internal = new JEditorPane("text/html", "");

  private final JCheckBox[] features = new JCheckBox[Dictionary.NAMES.length];

  private final Dictionary dictionary = new Dictionary.Builder().build();

  private Word[] strictResult;
  private Word[] headResult;
  private Word[] tailResult;
  private Word[] internalResult;

  private ExecutorService exec = Executors.newFixedThreadPool(3);

  private RhymeFinder(final boolean isApplet) {
    super("RhymeFinder");
    setDefaultCloseOperation(isApplet ? JFrame.HIDE_ON_CLOSE : JFrame.EXIT_ON_CLOSE);

    final ActionListener findAction = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (strict.isEnabled() && head.isEnabled() && tail.isEnabled() && internal.isEnabled()) {
          strict  .setEnabled(false);
          head    .setEnabled(false);
          tail    .setEnabled(false);
          internal.setEnabled(false);
          repaint();
          findRhyme();
          showResult();
        }
      }
    };

    final ActionListener showAction = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        showResult();
      }
    };

    final JPanel north = new JPanel(new GridLayout(1, 2));
    {
      queryMora.addActionListener(findAction);
      blacklist.addActionListener(showAction);

      north.add(queryMora);
      north.add(blacklist);
    }

    final JPanel center = new JPanel(new GridLayout(1, 4));
    {
      strict  .setEditable(false);
      head    .setEditable(false);
      tail    .setEditable(false);
      internal.setEditable(false);

      center.add(new JScrollPane(strict,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
      center.add(new JScrollPane(head,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
      center.add(new JScrollPane(tail,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
      center.add(new JScrollPane(internal,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    }

    final JPanel south = new JPanel(new GridLayout(8, 4));
    {
      for (int i = 0, size = Dictionary.NAMES.length; i < size; ++i) {
        features[i] = new JCheckBox(Dictionary.NAMES[i]);
        features[i].addActionListener(showAction);
        south.add(features[i]);
      }
    }

    final JPanel main = new JPanel(new BorderLayout());
    main.add(north , BorderLayout.NORTH );
    main.add(center, BorderLayout.CENTER);
    main.add(south , BorderLayout.SOUTH );
    add(main);
    pack();
  }

  private void findRhyme() {
    final String queryText = this.queryMora.getText().trim();
    if (queryText.isEmpty()) {
      return;
    }

    final Mora[] queryMora = Word.toMora(queryText);
    if (queryMora.length <= 0) {
      return;
    }

    final Word[] strictResult   = dictionary.match        (queryMora);
    final Word[] headResult     = dictionary.matchPrefix  (queryMora);
    final Word[] tailResult     = dictionary.matchPostfix (queryMora);
    final Word[] internalResult = dictionary.matchInternal(queryMora);

    this.strictResult   = strictResult;
    this.tailResult     = tailResult;
    this.headResult     = headResult;
    this.internalResult = internalResult;
  }

  private int getFeature() {
    int feature = 0;
    for (int i = 0, size = features.length; i < size; ++i) {
      if (features[i].isSelected()) {
        feature |= (1 << i);
      }
    }
    return feature;
  }

  private void showResult() {
    final Mora[] blacklist = Word.toMora(this.blacklist.getText().trim());
    final int feature = this.getFeature();

    final String beginHtml = "<html><head></head><body><dl>";
    final String endHtml = "</dl></body></html>";

    // 押韻
    if (strictResult != null) {
      exec.execute(new Runnable() {
        @Override
        public void run() {
          final StringBuilder builder = new StringBuilder(beginHtml);
          for (final Word word : strictResult) {
            if (!word.contains(blacklist)) {
              builder.append(word.toHTML(feature));
            }
          }
          builder.append(endHtml);
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              strict.setEnabled(true);
              strict.setText(builder.toString());
              strict.setCaretPosition(0);
              strict.repaint();
            }
          });
        }
      });
    } else {
      strict.setEnabled(true);
    }

    // 頭韻
    if (headResult != null) {
      exec.execute(new Runnable() {
        @Override
        public void run() {
          final StringBuilder builder = new StringBuilder(beginHtml);
          for (final Word word : headResult) {
            if (!word.contains(blacklist)) {
              builder.append(word.toHTML(feature));
            }
          }
          builder.append(endHtml);
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              head.setEnabled(true);
              head.setText(builder.toString());
              head.setCaretPosition(0);
              head.repaint();
            }
          });
        }
      });
    } else {
      head.setEnabled(true);
    }

    // 脚韻
    if (tailResult != null) {
      exec.execute(new Runnable() {
        @Override
        public void run() {
          final StringBuilder builder = new StringBuilder(beginHtml);
          for (final Word word : tailResult) {
            if (!word.contains(blacklist)) {
              builder.append(word.toHTML(feature));
            }
          }
          builder.append(endHtml);
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              tail.setEnabled(true);
              tail.setText(builder.toString());
              tail.setCaretPosition(0);
              tail.repaint();
            }
          });
        }
      });
    } else {
      tail.setEnabled(true);
    }

    // 中間韻
    if (internalResult != null) {
      exec.execute(new Runnable() {
        @Override
        public void run() {
          final StringBuilder builder = new StringBuilder(beginHtml);
          for (final Word word : internalResult) {
            if (!word.contains(blacklist)) {
              builder.append(word.toHTML(feature));
            }
          }
          builder.append(endHtml);
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              internal.setEnabled(true);
              internal.setText(builder.toString());
              internal.setCaretPosition(0);
              internal.repaint();
            }
          });
        }
      });
    } else {
      internal.setEnabled(true);
    }
  }

  public static void main(String[] args) throws Exception {
    showFrame(new RhymeFinder(false));
  }

  public static final class Applet extends java.applet.Applet {
    private final JFrame frame = new RhymeFinder(true);

    public Applet() {
      setLayout(new BorderLayout());
      final JButton button = new JButton("RhymeFinder");
      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          showFrame(frame);
        }
      });
      add(button);
    }
  }

  private static void showFrame(final JFrame frame) {
    if (SwingUtilities.isEventDispatchThread()) {
      frame.setVisible(true);
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          frame.setVisible(true);
        }
      });
    }
  }
}

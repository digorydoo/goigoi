# TODO

* Bug: Explanations have furigana, but it isn't shown properly

* Bug: Asynchronous reset stats/set fake stats can garble stats data since stats are not thread-safe! The dialogue
  window should stay open until the task is done.

* When dropping a word, it should always drop the one with the best score, not necessarily the one being presented
  (but never the super progressive head).

* When hint has "noun", tr_en should have -ing rather than "to ~"

* Katakana font should be larger when word is short

* Add a tool that checks jisho db if none of kanjis in dont_confuse index groups are ambiguously used in jisho words

* Tategaki view should treat numbers specially (unless their furigana is broken in parts)
    - single digit numbers should use wide chars
    - two-digit numbers should use normal-width chars and occupy one space
    - three- and four-digit numbers should use condensed width chars and occupy one space

* Tategaki view: Wavy dash 〰 (nami) should also be rotated

* If there is more than one sentence/phrase, it should not pick a sentence/phrase whose level is more difficult than
  the word it's in, except if the word's rating is fairly high

* It should somehow mark the answer when accepting a synonym instead of the actual word, e.g. use (!) icon

* Allow hints in phrases and sentences (e.g. to mark ガソリンスタンド as a noun), but restrict them to known hints

* When studyInContext="preferred", should still check how many phrases actually can be used

* If presented characters and answer are 1:1, then characters should be disabled once they're typed, and enter
  key should be disabled until all characters have been typed (= if length of answer equals length of input)

* Show streak in app bar

* Can we avoid calling :app:lintReportJapanese_freeDebug on every build?

* Dakuten of Katakana get clipped at right end with fat font, e.g. キャンプ

* Text size is too large for Krankenhauseinweisung (breaks word)

* Store date of study per word; use that when deciding which word to pick from the past

* Do not show extended keyboard if word is usuallyInKana, but question contains kanji. (Since the word is not
  usually written in kanji, the kanji variant is likely to be rare, and there may be another reading that is
  common, which will be rejected by the question.)

* Empty answer should be prevented.

* Also accept synonyms when asking, but use exclamation icon with a warning

* Add <avoid suggestion="kanaOrKanji" rem="otherwise user could type synonym blah" />

* Buttons of keyboard should be larger if there are <= 4 buttons and all of them have <= 3
  characters (e.g. 新聞社)

* Store date along with words, and use that to determine which word to pick when picking from the past

* Unyts with progress == 0 should have a lock icon, and clicking them should show a message like, "You have not
  reached this level yet."

* After a correct answer, occasionally show word links such as "same reading", "antonym", "noun/verb", but only for
  words whose level is less or equal the current word's + 1; when there are more than one such link, show them one by
  one

* When SHOW_WORD_ASK_NOTHING was shown once for word, we could now show a MATCH_WORDS_WITH_TRANSLATIONS along with
  three other words. This new kind is shown only once for the word. Criteria: translations must not exceed a certain
  length; SHOW_WORD_ASK_NOTHING must have been shown for the other words, too; numCorrect of the other words must be
  < 3.

* Show a button "I know that word" next to the (i) button in the following situation: answer was correct, and
  3 >= numSeen >= 5, and numCorrect == numSeen, and word is not the super progressive index. When the user clicks
  the button, it should increment numCorrect once more, move the word to the far back of StudySet, and remove it
  from My Words list.

* Words linked with noun--verb should also be treated like keep_apart in StudyItemProvider.

* Image of large button should have a glowing outline or maybe flickering light, to make it pop out even more

* BottomSheet should load and navigate to other words when see also link is clicked

* ProgStudy: When the explanation appears, the (i) icon should still be there

* The first three times the extended keyboard is shown, key buttons containing answer char should
  glow one by one. On error, the backspace should glow. The buttons should glow at any time when
  it's the first time the user has to pick one of: ん, dakuten, handakuten, ー

* Get rid of generic hints "verb", "adjective"; use "v.t." or "na-adjective" instead

* Move sentences and phrases that are too many (FIXMEs)

* Add <see rem="synonym"> to words that have the same meaning (don't use "closely related")

* outdated words like denpō should be N3 or less

* Add constraint that non-hidden word should never come without hint (but attribute may be left empty)

* knownSurnames and knownFirstnames should actually check the kanjis used

* When asking kana for a word or phrase containing kanji, it should pre-fill kana prefix and suffix, because user
  will simply have to copy them from the question. Prefix and suffix should not be editable; editable text should
  use a different colour

* If answer consists of hiragana+katakana and no kanji, allow any kind of kana as answer

* Should take kanjiIndex into account when determining char difficulty

* Implement hint_* for phrases and sentences; only allow fixed hints to avoid confusion with explanation

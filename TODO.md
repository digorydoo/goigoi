# TODO

* Bug: Asynchronous reset stats/set fake stats can garble stats data since stats are not thread-safe! The dialogue
  window should stay open until the task is done.

* Bug: Word info bottom sheet is clipped by corners even when scrolled beyond the top

* Bug: Sometimes it is possible to draw the bottom sheet when it should be scrolled

* Bug: Flip thru font size is too large, and word is not properly centred

* UnytListItem and WordListItem: Secondary text should be in Japanese (number of items/date studied)

* Big study button's background image should be lighter in light mode and should suggest daytime; maybe even reposition
  objects

* Achieving daily goal should unlock a background image for BigRing. Text should NOT change its colour; make two
  variants of each image instead.

    * 日: solid red in dark mode, solid yellow in light mode
    * 月: almost Neumond in dark mode, almost Vollmond in light mode
    * 火: red fire in dark mode, yellow fire in light mode; fire is repeated pattern based on known one
    * 水: below water level with bubbles in dark mode; above water level with waves in light mode; OR wave painting
    * 木: bark of a tree; different trees in dark or light; OR, tree painting based on typical art style
    * 金: ? golden Buddha statue? DO THIS ONE FIRST
    * 土: front view with brown ground and ghibli-like clouds

* Pencil might have a light and a shadow side

* Unyt and Word icons should show gauge-like progress instead of ring

* Allow defining the daily goal in prefs (just LOW, NORMAL, HIGH)

* Prefs: Instead of a Switch, dark mode should have three modes: AUTO, LIGHT, DARK; default should be AUTO

* Allow tapping the progress icon in prog study to show daily progress, num correct, num wrong, max streak

* Studying N5 words: For some reason, phrases and sentences are extremely rarely asked

* Save and restore state when orientation changes in ProgStudyActivty

* Bottom sheet should have actions again, at least the fake stats actions

* Train a model to recognize hand-drawn kanji; use different fonts and slightly different sizes for training. Train the
  official kanji list per schoolyear; maybe even more to make sure incorrect kanji are not accepted. Use that for a new
  QAKind that shows for single-kanji words.

* Remove furigana dots in TategakiView when furigana spans more than one kanji. It is rather unusual.

* Add left/right buttons to extended keyboard

* Compiler should reject phrases whose <ask> form is the same as the phrase

* If phrase is shown of a word whose hint is "formal", "honorific", "extra-modest", etc., the hint should include that
  with the phrase as well.

* Flip Thru should not have their own stats, because Prog Study cannot affect score enough if Flip Thru score is bad.
  Instead, Flip Thru should use the Prog Study stats key based on what was asked on the frontside of the card.

* There is currently no mode that allows me to study kanji memory actively, i.e. always show the kana/translation, I
  need to know the kanji without being presented a choice. This is only possible in Flip Thru (unless we implement
  drawing a kanji.) Therefore, Flip Thru should have Study Mode back, telling what to show on the frontside of the card.
  The backside is always the same (everything).

* Words that should be spreaded out among JLPT level in prog study ordering should not do so with weights. Instead,
  first let them be ordered like other words, then move them out in that order into a separate list, and put them back
  in spreaded manner. This should be done for loanwords, numbers, months of the year, etc. one by one. The distance
  between words of the last category added like this will be even, while those of previous categories will be slightly
  off due to other words being pushed in between, but this shouldn't be a problem.

* We could show origin in bottom sheet if it's one of the Manga or GENKI.

* When picking word from past, the study moment should be taken into account

* Allow clicking phrases and sentences in bottom sheet to open jisho with those as the query.

* Compiler: When hint has "noun; suru verb", tr_en should have -ing rather than "to ~"

* Add a tool that checks jisho db if none of kanjis in dont_confuse index groups are ambiguously used in jisho words

* Tategaki view: Wavy dash 〰 (nami) should also be rotated

* Tategaki view should treat numbers specially (unless their furigana is broken in parts)
    - single digit numbers should use wide chars
    - two-digit numbers should use normal-width chars and occupy one space
    - three- and four-digit numbers should use condensed width chars and occupy one space

* FixedKeys: If each character in answer appears exactly once, and keyboard does not show additional characters, then
  characters should be disabled once they're typed, and enter key should be disabled until all characters have been
  typed (= if length of answer equals length of input)

* Synonyms should only be used when asking word, but inside phrase and sentences, they should need explicit declaration

* It should somehow mark the answer when accepting a synonym instead of the actual word, e.g. use (!) icon

* Use word's studyMoment when determining which word to pick when picking from the past

* Add <avoid suggestion="kanaOrKanji" rem="otherwise user could type synonym blah" /> to limit kana and kanji
  suggestions of FixedKeys

* Implement hint_* for phrases and sentences (e.g. to mark ガソリンスタンド as a noun); only allow fixed hints to avoid
  confusion with explanation

* If there is more than one sentence/phrase, it should not pick a sentence/phrase whose level is more difficult than the
  word it's in, except if the word's rating is fairly high

* After a correct answer, occasionally show word links such as "same reading", "antonym", "noun/verb", but only for
  words whose level is less or equal the current word's + 1; when there are more than one such link, show them one by
  one

* Should take kanjiBySchoolYear into account when determining char difficulty

* Dakuten of Katakana get clipped at right end with fat font, e.g. キャンプ, トランプ (still with Compose)

* Text size is too large for Krankenhauseinweisung (breaks word)

* Get rid of generic hints "verb", "adjective"; use "v.t." or "na-adjective" instead

* Do not show extended keyboard if word is usuallyInKana and asked as kana, but word in question is shown as kanji.
  (Since the word is not usually written in kanji, the kanji variant is likely to be rare, and there may be another
  reading that is common, which will be rejected by the question.)

* Should we bring back hints for extended keyboard when it is first shown? (Dropped with ProgStudyActivity rewrite.)

* The first three times the extended keyboard is shown, key buttons containing answer char should glow one by one. On
  error, the backspace should glow. The buttons should glow at any time when it's the first time the user has to pick
  one of: ん, dakuten, handakuten, ー

* Unyts with progress == 0 should have a lock icon, and clicking them should show a message like, "You have not reached
  this level yet."

* When SHOW_WORD_ASK_NOTHING was shown once for word, we could now show a MATCH_WORDS_WITH_TRANSLATIONS along with three
  other words. This new kind is shown only once for the word. Criteria: translations must not exceed a certain length;
  SHOW_WORD_ASK_NOTHING must have been shown for the other words, too; numCorrect of the other words must be
  < 3.

* Words linked with noun--verb should also be treated like keep_apart in StudyItemProvider.

* Image of large button could have a glowing outline or maybe flickering light, to make it pop out even more

* BottomSheet should load and navigate to other words when see also link is clicked

* Move sentences and phrases that are too many (FIXMEs)

* knownSurnames and knownFirstnames should actually check the kanjis used

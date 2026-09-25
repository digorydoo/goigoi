# TODO

* Bug: Asynchronous reset stats/set fake stats can garble stats data since stats are not thread-safe! The dialogue
  window should stay open until the task is done.

* Bug: Word info bottom sheet is clipped by corners even when scrolled beyond the top

* Bug: Sometimes it is possible to draw the bottom sheet when it should be scrolled

* Save and restore state when orientation changes in ProgStudyActivty

* Hide the InstructionHint on small screens or normal landscape as before

* Bottom sheet should have actions again, at least the fake stats actions

* If answer consists of hiragana+katakana and no kanji, allow any kind of kana as answer; especially because ペ cannot
  be distinguished visually between katakana and hiragana

* Remove furigana dots in TategakiView when furigana spans more than one kanji. It is rather unusual.

* Add left/right buttons to extended keyboard

* Compiler should reject phrases whose <ask> form is the same as the phrase

* If phrase is shown of a word whose hint is "formal", "honorific", "extra-modest", etc., the hint should include that
  with the phrase as well.

* Flip Thru should not have their own stats, because Prog Study cannot affect score enough if Flip Thru score is bad.
  Instead, Flip Thru should use the Prog Study stats key based on what was asked on the frontside of the card.

* There is currently no mode that allows me to study kanji memory actively, i.e. always show the kana/translation, I
  need to know the kanji without being presented a choice. This is only possible in Flip Thru (unless we implement
  drawing a kanji.) Therefore, Flip Thru should have Study Mode back, telling what to show on the frontside of the
  card. The backside is always the same (everything).

* We could show origin in bottom sheet if it's one of the Manga or GENKI.

* Show streak in header

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

* If there is more than one sentence/phrase, it should not pick a sentence/phrase whose level is more difficult than
  the word it's in, except if the word's rating is fairly high

* After a correct answer, occasionally show word links such as "same reading", "antonym", "noun/verb", but only for
  words whose level is less or equal the current word's + 1; when there are more than one such link, show them one by
  one

* Should take kanjiBySchoolYear into account when determining char difficulty

* Dakuten of Katakana get clipped at right end with fat font, e.g. キャンプ; is this still true with Compose?

* Text size is too large for Krankenhauseinweisung (breaks word)

* Get rid of generic hints "verb", "adjective"; use "v.t." or "na-adjective" instead

* Do not show extended keyboard if word is usuallyInKana and asked as kana, but word in question is shown as kanji.
  (Since the word is not usually written in kanji, the kanji variant is likely to be rare, and there may be another
  reading that is common, which will be rejected by the question.)

* Should we bring back hints for extended keyboard when it is first shown? (Dropped with ProgStudyActivity rewrite.)

* The first three times the extended keyboard is shown, key buttons containing answer char should
  glow one by one. On error, the backspace should glow. The buttons should glow at any time when
  it's the first time the user has to pick one of: ん, dakuten, handakuten, ー

* Unyts with progress == 0 should have a lock icon, and clicking them should show a message like, "You have not
  reached this level yet."

* When SHOW_WORD_ASK_NOTHING was shown once for word, we could now show a MATCH_WORDS_WITH_TRANSLATIONS along with
  three other words. This new kind is shown only once for the word. Criteria: translations must not exceed a certain
  length; SHOW_WORD_ASK_NOTHING must have been shown for the other words, too; numCorrect of the other words must be
  < 3.

* Words linked with noun--verb should also be treated like keep_apart in StudyItemProvider.

* Image of large button could have a glowing outline or maybe flickering light, to make it pop out even more

* BottomSheet should load and navigate to other words when see also link is clicked

* Move sentences and phrases that are too many (FIXMEs)

* knownSurnames and knownFirstnames should actually check the kanjis used

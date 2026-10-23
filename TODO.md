# TODO

* Bug: CorrectedAnswer has zero spacing between lines when it wraps (showed kana-only answer)

* Missing dakuten/handakuten should also be "almost correct". Use equalsExceptDakutenHandakuten to check.

* If <ask> is the same as the phrase, e.g. with phrase kakkoii inside word kakkō, SHOW_PHRASE_ASK_* should not be shown

* SHOW_PHRASE_ASK_WORD_KANJI should show kana as a second step (kamawanai)

-----

* When there is at least one phrase, sentences should not be shown before at least one phrase has been shown

* Save and restore state when orientation changes in ProgStudyActivty

* When answer is a verb with furigana, and showing choices with fixed keys, we should not simply pick other words from
  the list as the wrong answers, but: (i) find one okurigana that ought to be wrong (not part of the masu-pair), and
  replace the okurigana with that; (ii) pick a random kanji, ideally from another verb of the study set, and add the
  original okurigana; (iii) the constructed word with both parts wrong.

* Bottom sheet should have actions again, at least the fake stats actions

* My Words unyt should be called like this again, because "Add to My Words" still exists.

* FAB of UnytActivity should replace words in My Words unyt with the words from selected unyt, and then start
  progressive mode. No flip-through any more! Should show dialogue: "Start learning this lesson?" No need to mention
  that My Words get overwritten as that list is automatically maintained anyway.

* My Words unyt should have a notice at the top: "This is the list of words you're currently studying. It is
  automatically updated as you progress. You can manually add or remove words to control what you want to study next."

* Instead of showing underlines, answer and question fields should merge when answer is part of the question text.

* Make topic subheaders more visible by moving them into separate files. We should use the same instance of
  GoigoiXmlParser to read the files in order to support this.

* Can we resolve topic subheaders in app at loading time instead of having to detect them when building the list?

* Make an abstraction of the underlying XmlParser, moving all helper functions there. Then, break GoigoiXmlParser into
  VocabXmlParser and KanjiIndexXmlParser.

* All checks should happen when everything has been parsed, and none should happen after word files have been written.

* All words which are basically just the stem of a verb should have studyInContext=preferred, e.g. kari, tabe, nomi,
  etc.

* Apparently, words from My Words are simply added to the study set at launch without checking for ambiguities. This can
  be a problem if the user added those manually.

* When picking word from past, the study moment should be taken into account: Find a list of candidates just like now,
  then sort the candidates by study moment and then take the half that is further into the past.

* FixedKeys: If each character in answer appears exactly once, and keyboard does not show additional characters, then
  characters should be disabled once they're typed, and enter key should be disabled until all characters have been
  typed (= if length of answer equals length of input)

* Add <accept_for_kanji> to words, phrases, sentences, e.g. 入れる could be read as いれる or はいれる

* UnytListItem and WordListItem: Secondary text should be in Japanese (number of items/date studied)

* If phrase is shown of a word whose hint is "formal", "honorific", "extra-modest", etc., the hint should include that
  with the phrase as well.

* Words that should be spreaded out among JLPT level in prog study ordering should not do so with weights. Instead,
  first let them be ordered like other words, then move them out in that order into a separate list, and put them back
  in spreaded manner. This should be done for loanwords, numbers, months of the year, etc. one by one. The distance
  between words of the last category added like this will be even, while those of previous categories will be slightly
  off due to other words being pushed in between, but this shouldn't be a problem.

* We could show origin in bottom sheet if it's one of the Manga or GENKI.

* Allow clicking phrases and sentences in bottom sheet to open jisho with those as the query.

* Compiler: When hint has "noun; suru verb", tr_en should have -ing rather than "to ~"

* Prefs: Allow defining the daily goal in prefs (just LOW, NORMAL, HIGH)

* Add a tool that checks jisho db if none of kanjis in visually_similar groups are ambiguously used in jisho words

* Tategaki view: Wavy dash 〰 (nami) should also be rotated

* Tategaki view should treat numbers specially (unless their furigana is broken in parts)
    - single digit numbers should use wide chars
    - two-digit numbers should use normal-width chars and occupy one space
    - three- and four-digit numbers should use condensed width chars and occupy one space

* Add <avoid suggestion="kanaOrKanji" rem="otherwise user could type synonym blah" /> to limit kana and kanji
  suggestions of FixedKeys

* Use word's studyMoment when determining which word to pick when picking from the past

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

* When SHOW_WORD_ASK_NOTHING was shown once for a given word, we could now show a MATCH_WORDS_WITH_TRANSLATIONS along
  with three other words. This new kind is shown only once for the word. Criteria: translations must not exceed a
  certain length; SHOW_WORD_ASK_NOTHING must have been shown for the other words, too; numCorrect of the other words
  must be < 3.

* Words linked with noun--verb should also be treated like keep_apart in StudyItemProvider.

* Image of large button could have a glowing outline or maybe flickering light, to make it pop out even more

* Big study button's background image should be brighter in light mode and should suggest daytime; maybe even reposition
  objects in the image

* Achieving daily goal should unlock a background image for BigRing. Text should NOT change its colour; make two
  variants of each image instead. 日: red sun, 月: blue moon, 火: orange fire pattern, 水: greenish water pattern, 木:
  bark of a tree; 金: ??? golden Buddha statue? DO THIS ONE FIRST; 土: ??? front view with brown ground?

* WelcomeActivity: Pencil might have a light and a shadow side

* BottomSheet should load and navigate to other words when see also link is clicked

* Move sentences and phrases that are too many (FIXMEs)

* knownSurnames and knownFirstnames should actually check the kanjis used

* Train a model to recognize hand-drawn kanji; use different fonts and slightly different sizes for training. Train the
  official kanji list per schoolyear; maybe even more to make sure incorrect kanji are not accepted. Use that for a new
  QAKind that shows for single-kanji words.

* If Flashcards mode (AKA "flip through") should ever come back, here's how it was implemented in an earlier version:
    * Frontside shows question, which contains of kanji/kana/translation, can have furigana, and question hint
    * Backside shows everything about the word including the question, but never explanation, never phrases, never
      sentences
    * Gesture towards left means CORRECT, pulls the card, and reveals a checkmark icon at the right-hand side. As the
      gesture continues, the icon moves towards the centre of the screen. The icon stays for a couple of milliseconds
      when the card is fully flung out of the screen boundaries, and then also follows the same direction and vanishes
      at the left screen border.
    * Gesture towards right means WRONG, pulls the card, and reveals a flash icon at the left-hand side
    * Gesture towards the top means SKIP, pulls the card, and reveals the text "SKIP"
    * Gesture towards the bottom means INFORMATION, pulls the card, and reveals the text "INFORMATION"
    * The INFORMATION gesture opens the bottom sheet. The INFORMATION label stays in the centre behind the bottom sheet.
      When the bottom sheet is dismissed, the label drop towards the bottom of the screen.
    * When frontside is shown, gestures left and right are prevented: allow pulling it a little, but the further out the
      gesture goes, the less effect it has on the pull
    * When tapping the frontside, the card is flipped, which was implemented with a crossfade of the card content (the
      card itself does not change as it always has the same size disregarding content).
    * When tapping the backside, the card just shook. The shake direction altered between up/down and left/right. The
      shake distance was just large enough to reveal a little from the icons/texts underneath the card.
    * In the last stage of the implementation, the words were shuffled initially, then shown one by one. When the list
      repeated, a snackbar appeared.
    * What we could do better in a new implementation:
        * Allow choosing between:
            * Show kanji and nothing else (no furigana; kana if kanji not available): Lets the user focus on reading as
              well as translation
            * Show kana and nothing else: Lets the user focus on thinking how they would write the kanji (ProgStudy
              currently cannot cover this use case, because it always gives away something by showing kanji in
              multiple-choice fashion)
            * Show translation and question hint: Lets the user focus on their active vocabulary
        * Do not make an extra settings screen, just show these options in a modal dialogue as ListItems with primary
          and secondary texts. The secondary text describes the use case similar to the above.
        * Make the transition between frontside and backside nicer with an Oxyd-like paper folding effect and shadow
    * I hardly ever used FlipThrough after I created ProgStudyActivity, so maybe there's no need for it

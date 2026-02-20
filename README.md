# Go Go Japanese

## Overview

Codename: goigoi

This project consist of these parts:

* Android app Go Go Japanese
* Extensive Japanese vocabulary DB in XML (words, sentences, phrases, cross-links)
* goigoi-compiler, a command line tool to check the vocabulary XML and store it as a bunch of compact binary files

The app is not currently available on Google Play.

An iOS version of the app has not been implemented.

## How to build

Before building, you need to clone kutils from `git@github.com:digorydoo/kutils.git`. Unfortunately, kutils has not
been pushed to mavenCentral yet. Make a symbolic link from this project's kutils/src to kutils' src, e.g.:

    $ cd kutils
    $ ln -s ../../kutils/main/src/ src

If you're using Windows, symbolic links *could* be created (e.g. with Cygwin or PowerShell), but Gradle does not
support them, so you'll have to copy kutils sources into the target directory.

Now you can build the sources with:

    $ ./gradlew build

After building, the Android app won't start yet, because you need to generate the vocabulary files first. Run:

    $ ./compile-goigoi.sh

The command generates files under `./app/build/**/*.voc`. It also creates a file out_ja.txt, which should have the same
content as the one that was committed to git. Check if out_ja.txt was modified:

    $ git status

If out_ja.txt shows a diff, it's likely that something went wrong with compiling the vocabulary. If there's no diff,
you should now be able to run the app in Android Studio (simulator or real device).

## The Japanese vocabulary DB

Vocabulary files were handwritten by looking up the words in real dictionaries (Langenscheidt German-Japanese) as well
as online dictionaries (mostly jisho.org). The descriptions are *not* meant to be exhaustive. Rather, they focus on a
word's main meaning, typically giving two or three English synonyms. Finer granulated subtleties are provided with
phrases and sentences. Origin of phrases and sentences are usually indicated in the XML:

* 500mon: Learning books にほんご500門
* GENKI1/2: Learning books GENKI I/II
* Langenscheidt: Refers to two books, one of them is a Japanese-German dictionary, another is a learning guide
  aimed at travellers
* Oxford: Book "Oxford Japanese Grammar & Words"
* Doraemon: Manga books
* Kaname Naito: [Youtube channel](https://www.youtube.com/@kanamenaito/videos)
* Miku: [Youtube channel](https://www.youtube.com/channel/UCsQCbl3a9FtYvA55BxdzYiQ/videos)
* jpod101: [Youtube channel](https://www.youtube.com/c/japanesepod101/videos)
* Yuko Sensei: [Youtube channel](https://www.youtube.com/c/YukoSensei/videos)
* BondLingo: [Youtube channel](https://www.youtube.com/@bondlive-en/videos)
* lib0: There was a site called liberty-zero that provided free sample sentences. I can no longer find that site,
  but the sentences seem to stem from [Tatoeba](https://tatoeba.org), which is licensed under CC-BY 2.0 FR.
* tofugu: [Tofugu website](https://www.tofugu.com/japanese-grammar)
* self: I wrote these sentences myself.

## Legal status of included fonts

Go Go Japanese displays font copyright notices under AboutActivity. (From WelcomeActivity, press the gear icon to get
to PrefsActivity, and there you'll find the "About..." item to get to AboutActivity.)

Some of the fonts were stripped with `fonttools` for smaller download size.

### Dela Gothic One

I obtained Dela Gothic One from fonts.google.com, which refers to github.com/syakuzen/DelaGothic, which declares it
under Open Font License version 1.1, which in turn allows for redistribution of the font, PROVIDED that I show their
copyright notice somewhere inside the App. Which I currently don't: *TODO*

The font is quite large, so I stripped it down to just include katakana and some punctuation characters like this:

    $ brew upgrade
    $ pip install fonttools
    $ fonttools subset ~/Downloads/DelaGothicOne-Regular.ttf \
         --text='。、!！？「」・〜ー' \
         --unicodes="U+30a0-30ff" \
         --output-file="dela_gothic_one_katakana_stripped.woff" \
         --flavor=woff \
         --recommended-glyphs \
         --recalc-timestamp \
         --recalc-average-width \
         --recalc-max-context \
         --no-ignore-missing-glyphs \
         --ignore-missing-unicodes

Fonttools is documented here: https://fonttools.readthedocs.io/en/latest/subset/index.html#module-fontTools.subset

Unfortunately, fonttols can only output woff or woff2, but Android doesn't understand these formats, so I converted it
using fontforge:

    $ brew install fontforge
    $ fontforge -lang=ff -c 'Open($1); Generate($2)' \
        dela_gothic_one_katakana_stripped.woff app/src/res/font/dela_gothic_one_katakana_stripped.otf
    $ rm dela_gothic_one_katakana_stripped.woff

### Mochiy Pop One

I obtained Mochiy Pop One from fonts.google.com, which states it's licensed under Open Font License version 1.1. So
again, redistribution should be OK, PROVIDED that I show their copyright notice somewhere inside the App, which I
currently don't: *TODO*

I stripped the font to include hiragana only like this:

    $ fonttools subset ~/Downloads/Mochiy_Pop_One/MochiyPopOne-Regular.ttf \
      --text='。、!！？「」・〜ー' \
      --unicodes="U+3040-309f" \
      --output-file="mochiy_pop_one_hiragana_stripped.woff" \
      --flavor=woff \
      --recommended-glyphs \
      --recalc-timestamp \
      --recalc-average-width \
      --recalc-max-context \
      --no-ignore-missing-glyphs \
      --ignore-missing-unicodes \
      && fontforge -lang=ff -c 'Open($1); Generate($2)' \
      mochiy_pop_one_hiragana_stripped.woff app/src/res/font/mochiy_pop_one_hiragana_stripped.otf \
      && rm -f mochiy_pop_one_hiragana_stripped.woff

### Zen Kurenaido

I obtained Zen Kurenaido from fonts.google.com, which states it's licensed under Open Font License version 1.1. So
again, redistribution should be OK, PROVIDED that I show their copyright notice somewhere inside the App, which I
currently don't: *TODO*

### EPSON Tai Xing Shu Tib 2

I can't remember where I obtained it (I think it was some Japanese site), but the TTF contains the copyright notice,
which can be viewed with macOS Font Book:

(C) 2000 SEIKO EPSON CORP. All rights reserved.
EPSON * and EPSON-FUTO-GYOSHO is a trademark of SEIKO EPSON CORPORATION.
No embedding restrictions.

It's quite large, and its name suggest it also contains Chinese characters, but it's unclear what range to crop without
losing something I need. When trying to use fontforge to convert it to OTF, it ended up with an even larger size, and
fontforge also emits a couple of warnings. So, even if I did know the range to crop (which I could have compileGoigoi
figure out), it's unclear whether the result would be good. For now, I keep the entire font.

## Names appearing in example sentences

Sentences sometimes use fictional names to give them some context. They do not refer to real-life persons.

* (11) 石川さん - Mr Ishikawa (elderly person (70), talks little, has a garden, listens to music)
* ( 7) 古川さん - Mrs Furukawa (elderly person, likes knitting, cheerful)
* (15) 山田さん - Mr Yamada (businessman, has a red beard, likes jazz, does research on new tech)
* (14) 中村さん - Mrs Nakamura (businesswoman, likes to cook, has an expensive car)
* (10) 高木さん - Mr Takagi (businessman, works at a bank, likes fishing)
* ( 5) 谷口さん - Mrs Taniguchi (lawyer, not a good cook, has a husband)
* ( 8) 小山さん - Mrs Koyama (likes clothes, has a big nose, work for foreign trade)
* (23) 田中さん - Mr Tanaka (bus driver), Mrs Tanaka, Ms Tanaka
* ( 7) 森田さん - Mr Morita (construction worker; likes diy; has three children)
* ( 8) 金子さん - Mr Kaneko (28, no money, has a cat, hikikomori)
* (11) 池田さん - Ms Ikeda (28, housewife, has a horse, likes shopping, cannot speak EN)
* (11) 西村さん - Mr Nishimura (25, likes sports, graduated from university, friends with Nakagawa)
* (13) 中川さん - Ms Nakagawa (25, likes books, quit her job for writing, friends with Nishimura)
* ( 7) 小川さん - Mr Ogawa Ken (24, studies law, can speak a little Chinese, has a calm attitude)
* ( 9) 小林さん - Mr Kobayashi (23, university student, unreliable)
* (12) 木村さん - Miss Kimura (23, likes sports, has a strict mother)
* (10) 山下さん - Mr Yamashita (19, likes going abroad, doesn't read books)
* (13) 高橋さん - Miss Takahashi (18, stylish, singer)
* ( 6) 山口先生 - Professor/Mr Yamaguchi (at university)
* ( 7) 青木先生 - Professor/Mrs Aoki (headmaster at a high school; teaches English)
* (11) ナオミ（さん） - Naomi (w., exchange student from America)
* (10) ハルト（さん） - Haruto (m., failed to enter university)
* (11) カオル（さん） - Kaoru (w.), wants to study literature, plays the guitar, piano
* ( 8) ヨシ（さん） - Yoshi (m., cousin of Miki, middleschool student, bully)
* ( 8) ミキ（さん） - Miki (w., cousin of Yoshi, middleschool, diligent)
* ( 8) マリ（さん） - Mari Ikeda (w., daughter of Ms Ikeda, not good at sports)
* ( 9) レオ（くん） - Reo (m.; junior, likes comics, his father is a doctor)
* ( 8) リサ（ちゃん） - Risa (w.; child, likes chocolate)

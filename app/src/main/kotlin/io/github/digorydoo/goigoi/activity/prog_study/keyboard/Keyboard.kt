package io.github.digorydoo.goigoi.activity.prog_study.keyboard

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.Bindings
import io.github.digorydoo.goigoi.activity.prog_study.Values
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.Mode.FIXED_KEYS
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.Mode.HIRAGANA
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.Mode.JUST_REVEAL
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.Mode.KATAKANA
import io.github.digorydoo.goigoi.utils.addHapticFeedback
import ch.digorydoo.kutils.cjk.isSmallKana
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class Keyboard(
    private val delegate: Delegate,
    private val bindings: Bindings,
    private val values: Values,
) {
    enum class Mode { JUST_REVEAL, FIXED_KEYS, HIRAGANA, KATAKANA }
    enum class KeyLensPart { CENTRE, LEFT, ABOVE, RIGHT, BELOW }

    abstract class Delegate {
        abstract val chipFontSize: Float
        abstract val chipFontSizeForSmallKana: Float
        abstract fun createNewChip(iconResId: Int?, contentDescResId: Int?): Chip
        abstract fun createNewChipGroup(): ChipGroup
        abstract fun createNewButton(strResId: Int): View
        abstract fun createNewKeyLensDrawable(keyDef: KeyDef): KeyLensDrawable
        abstract fun applyInputTextTransform(trf: (text: String) -> String)
        abstract fun okBtnClicked()
    }

    var mode = JUST_REVEAL; private set
    private var backspaceClearsAllText = false
    private var keyLens: KeyLensDrawable? = null

    fun supportsCharsInMode(chars: String, mode: Mode) = when (mode) {
        JUST_REVEAL -> false
        FIXED_KEYS -> true
        HIRAGANA -> chars.all { KeyDef.supportedHiraganaAndPunctuation.contains(it) }
        KATAKANA -> chars.all { KeyDef.supportedKatakanaAndPunctuation.contains(it) }
    }

    fun setMode(mode: Mode, keys: List<String>? = null, backspaceClearsAllText: Boolean = false) {
        this.mode = mode
        this.backspaceClearsAllText = backspaceClearsAllText
        bindings.keyboardView.removeAllViews()

        when (mode) {
            JUST_REVEAL -> {
                addChipGroup().let {
                    addRevealBtn(it)
                }
            }
            FIXED_KEYS -> {
                addChipGroup().let { group ->
                    keys?.forEach { addKeyChip(group, it) }
                }
                addChipGroup().let {
                    addActionChip(it, R.drawable.ic_backspace_24dp, R.string.backspace_key) {
                        backspaceBtnClicked()
                    }
                    addActionChip(it, R.drawable.ic_enter_white_24dp, R.string.enter_key, true) {
                        delegate.okBtnClicked()
                    }
                }
            }
            else -> {
                addChipGroup().let {
                    addInvisibleChip(it)
                    addKeyChip(it, if (mode == HIRAGANA) KeyDef.hiraganaVowelKey else KeyDef.katakanaVowelKey)
                    addKeyChip(it, if (mode == HIRAGANA) KeyDef.hiraganaKxKey else KeyDef.katakanaKxKey)
                    addKeyChip(it, if (mode == HIRAGANA) KeyDef.hiraganaSxKey else KeyDef.katakanaSxKey)
                    addActionChip(it, R.drawable.ic_backspace_24dp, R.string.backspace_key) {
                        backspaceBtnClicked()
                    }
                }

                addChipGroup().let {
                    addKeyChip(it, if (mode == HIRAGANA) KeyDef.hiraganaTxKey else KeyDef.katakanaTxKey)
                    addKeyChip(it, if (mode == HIRAGANA) KeyDef.hiraganaNxKey else KeyDef.katakanaNxKey)
                    addKeyChip(it, if (mode == HIRAGANA) KeyDef.hiraganaHxKey else KeyDef.katakanaHxKey)
                }

                addChipGroup().let {
                    addKeyChip(it, if (mode == HIRAGANA) KeyDef.hiraganaMxKey else KeyDef.katakanaMxKey)
                    addKeyChip(it, if (mode == HIRAGANA) KeyDef.hiraganaYxKey else KeyDef.katakanaYxKey)
                    addKeyChip(it, if (mode == HIRAGANA) KeyDef.hiraganaRxKey else KeyDef.katakanaRxKey)
                }

                addChipGroup().let {
                    addInvisibleChip(it)
                    addKeyChip(it, KeyDef.transformsKey)
                    addKeyChip(it, if (mode == HIRAGANA) KeyDef.hiraganaWxKey else KeyDef.katakanaWxKey)
                    addKeyChip(it, KeyDef.punctuationsKey)
                    addActionChip(it, R.drawable.ic_enter_white_24dp, R.string.enter_key, true) {
                        delegate.okBtnClicked()
                    }
                }
            }
        }
    }

    private fun addChipGroup() =
        delegate.createNewChipGroup().also { bindings.keyboardView.addView(it, bindings.keyboardView.childCount) }

    private fun addKeyChip(group: ChipGroup, chipText: String) =
        addKeyChip(group, KeyDef(chipText))

    private fun addKeyChip(group: ChipGroup, def: KeyDef) {
        val chipText = if (def.mainIconResId != null) "" else def.mainText

        val chip = delegate.createNewChip(def.mainIconResId, def.contentDescResId).apply {
            if (chipText.isNotEmpty()) {
                text = chipText

                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    if (chipText.length == 1 && chipText[0].isSmallKana()) {
                        delegate.chipFontSizeForSmallKana
                    } else {
                        delegate.chipFontSize
                    }
                )
            }

            addHapticFeedback()

            if (def.shouldShowLens) {
                val keyTouchDelegate = object: KeyTouchListener.Delegate {
                    override val keyLens get() = this@Keyboard.keyLens
                    override fun showKeyLensAbove(anchor: View) = this@Keyboard.showKeyLensAbove(anchor, def)
                    override fun hideKeyLens() = this@Keyboard.hideKeyLens()
                    override fun onKeyLensPartSelected(action: KeyDef.Action, key: String) =
                        this@Keyboard.onKeyLensPartSelected(action, key)
                }
                setOnTouchListener(KeyTouchListener(keyTouchDelegate, def, bindings, values))
            } else {
                setOnClickListener { onKeyLensPartSelected(KeyDef.Action.LITERAL, chipText) }
            }
        }
        group.addView(chip, group.childCount)
    }

    private fun addActionChip(group: ChipGroup, iconResId: Int, contentDescResId: Int, onClick: () -> Unit) =
        addActionChip(group, iconResId, contentDescResId, false, onClick)

    private fun addActionChip(
        group: ChipGroup,
        iconResId: Int,
        contentDescResId: Int,
        primaryColour: Boolean,
        onClick: () -> Unit,
    ) {
        val chip = delegate.createNewChip(iconResId, contentDescResId).apply {
            addHapticFeedback()
            setOnClickListener { onClick() }
            if (primaryColour) chipBackgroundColor = values.primaryColour.let { ColorStateList.valueOf(it) }
        }
        group.addView(chip, group.childCount)
    }

    private fun addInvisibleChip(group: ChipGroup) {
        // using ic_backspace just to get the correct size
        val chip = delegate.createNewChip(R.drawable.ic_backspace_24dp, null).apply {
            visibility = View.INVISIBLE
        }
        group.addView(chip, group.childCount)
    }

    private fun addRevealBtn(group: ChipGroup) {
        val btn = delegate.createNewButton(R.string.reveal_btn).apply {
            setOnClickListener { delegate.okBtnClicked() }
        }
        group.addView(btn, group.childCount)
    }

    private fun showKeyLensAbove(anchor: View, def: KeyDef) {
        // Anchor is the key that was pressed.
        val anchorScreenLoc = intArrayOf(0, 0)
        anchor.getLocationOnScreen(anchorScreenLoc)

        // Anchor is expected to be somewhere in the children of the keyboard.
        val keyboardScreenLoc = intArrayOf(0, 0)
        bindings.keyboardView.getLocationOnScreen(keyboardScreenLoc)

        // Compute the coords of the anchor relative to the keyboard.
        val anchorRelToKeyboardX = anchorScreenLoc[0] - keyboardScreenLoc[0]
        val anchorRelToKeyboardY = anchorScreenLoc[1] - keyboardScreenLoc[1] - values.keyLensVOffset

        // The desired location of the lens is centred above the anchor.
        val lensView = bindings.keyLensImageView
        val desiredLensRelToKeyboardX = anchorRelToKeyboardX + anchor.measuredWidth / 2 - lensView.measuredWidth / 2
        val desiredLensRelToKeyboardY = anchorRelToKeyboardY - lensView.measuredHeight

        // The lens' current location is irrelevant, but we can use it to compute the location of the parent.
        val lensCurScreenLoc = intArrayOf(0, 0)
        lensView.getLocationOnScreen(lensCurScreenLoc)
        val lensParentScreenX = lensCurScreenLoc[0] - lensView.left
        val lensParentScreenY = lensCurScreenLoc[1] - lensView.top

        // Now we can compute the new location relative to the lens parent.
        lensView.left = desiredLensRelToKeyboardX + keyboardScreenLoc[0] - lensParentScreenX
        lensView.top = desiredLensRelToKeyboardY + keyboardScreenLoc[1] - lensParentScreenY
        lensView.right = lensView.left + lensView.measuredWidth
        lensView.bottom = lensView.top + lensView.measuredHeight

        // The drawable is expected to be fit into the lensView's size, which should never change.
        keyLens = delegate.createNewKeyLensDrawable(def)
        bindings.keyLensImageView.setImageDrawable(keyLens)
        lensView.visibility = View.VISIBLE
        lensView.invalidate()
    }

    private fun onKeyLensPartSelected(action: KeyDef.Action, key: String) {
        delegate.applyInputTextTransform { TextTransformer().transform(it, action, key) }
    }

    private fun backspaceBtnClicked() {
        delegate.applyInputTextTransform {
            when {
                it.isEmpty() || backspaceClearsAllText -> ""
                else -> it.slice(0 ..< it.length - 1)
            }
        }
    }

    private fun hideKeyLens() {
        bindings.keyLensImageView.apply {
            visibility = View.INVISIBLE
            invalidate()
            setImageDrawable(null)
            keyLens = null
        }
    }
}

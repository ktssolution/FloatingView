package jp.co.recruit_lifestyle.android.floatingview.animation.listeners;

import android.animation.Animator;

import jp.co.recruit_lifestyle.android.floatingview.animation.enumerators.MenuState;
import jp.co.recruit_lifestyle.android.floatingview.animation.handlers.AnimationHandler;
import jp.co.recruit_lifestyle.android.floatingview.floatingmenubutton.subbutton.SubButton;


/**
 * Description
 *
 * @author <a href="mailto:ricardo.vieira@xpand-it.com">RJSV</a>
 * @version $Revision : 1 $
 */

public class FloatingMenuButtonAnimationListener implements Animator.AnimatorListener {

    private SubButton subActionItem;
    private MenuState actionType;
    private AnimationHandler animationHandler;

    public FloatingMenuButtonAnimationListener(AnimationHandler animationHandler, SubButton subActionItem, MenuState actionType) {
        this.subActionItem = subActionItem;
        this.actionType = actionType;
        this.animationHandler = animationHandler;
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        animationHandler.restoreSubActionViewAfterAnimation(subActionItem, actionType);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        animationHandler.restoreSubActionViewAfterAnimation(subActionItem, actionType);
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }
}
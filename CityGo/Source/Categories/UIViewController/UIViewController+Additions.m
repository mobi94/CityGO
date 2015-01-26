//
//  UIViewController+Additions.m
//  Gracefull
//
//  Created by ruslan on 1/7/15.
//  Copyright (c) 2015 NIX. All rights reserved.
//

#import "UIViewController+Additions.h"

@implementation UIViewController (Additions)

- (BOOL)isVisible
{
    return [self isViewLoaded] && self.view.window;
}

- (BOOL)isPresentedAsModal
{
    //iOS 5+
    BOOL isModal = NO;
    
    if (!isModal && [self respondsToSelector:@selector(presentingViewController)])
    {
        isModal = ((self.presentingViewController && self.presentingViewController.presentedViewController == self) ||
                   // or if I have a navigation controller, check if its parent modal view controller is self navigation controller
                   (self.navigationController && self.navigationController.presentingViewController && self.navigationController.presentingViewController.presentedViewController == self.navigationController) ||
                   // or if the parent of my UITabBarController is also a UITabBarController class, then there is no way to do that, except by using a modal presentation
                   [[[self tabBarController] presentingViewController] isKindOfClass:[UITabBarController class]]);
    }
    
    return isModal;
}

@end

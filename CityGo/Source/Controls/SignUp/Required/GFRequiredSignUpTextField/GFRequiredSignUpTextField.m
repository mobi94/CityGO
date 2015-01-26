//
//  GFRequiredSignUpTextField.m
//  Gracefull
//
//  Created by curly0nion on 9/19/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

#import "GFRequiredSignUpTextField.h"
#import "GFRequiredSignUpTextFieldProtected.h"
#import "CMPopTipView.h"

@interface GFRequiredSignUpTextField ()<CMPopTipViewDelegate>

@property(strong, nonatomic) CMPopTipView *popUp;

- (void)setupPopUp;
- (void)setupObserver;
- (void)removeObserver;

- (void)changeBackgroundColor:(UIColor *)color animated:(BOOL)animated;
- (void)showPopUpWithMessage:(NSString *)msg inView:(UIView *)view;
- (void)hidePopUp;

@end

@implementation GFRequiredSignUpTextField

#pragma mark -
#pragma mark Lifecycle

- (void)awakeFromNib
{
    [super awakeFromNib];
    
    [self setupPopUp];
    [self setupObserver];
}

- (void)dealloc
{
    [self removeObserver];
}

#pragma mark -
#pragma mark Custom setup

- (void)setupPopUp
{
    [self setPopUp:[[CMPopTipView alloc] initWithMessage:@""]];
    [_popUp setDelegate:self];
    [_popUp setPreferredPointDirection:PointDirectionDown];
    [_popUp setBackgroundColor:CG_GREEN_COLOR];
    [_popUp setTextFont:[UIFont fontWithName:CG_DEFAULT_FONT_NAME_MEDIUM size:CG_FONT_SIZE_SMALL]];
    [_popUp setTextColor:CG_WHITE_COLOR];
    [_popUp setTextAlignment:NSTextAlignmentLeft];
    [_popUp setAnimation:CMPopTipAnimationSlide];
    [_popUp setHasGradientBackground:NO];
    [_popUp setHas3DStyle:NO];
    [_popUp setBorderColor:[_popUp backgroundColor]];
    [_popUp setCornerRadius:4.0f];
    [_popUp setPointerSize:10.0f];
    [_popUp setHasShadow:YES];
    [_popUp setMaxWidth:[[UIScreen mainScreen] bounds].size.width * .8f];
}

- (void)setupObserver
{
    [self addObserver:self
           forKeyPath:@"valid"
              options:NSKeyValueObservingOptionNew
              context:nil];
}

- (void)removeObserver
{
    [self removeObserver:self forKeyPath:@"valid"];
}

#pragma mark -
#pragma mark CMPopTipViewDelegate

- (void)popTipViewWasDismissedByUser:(CMPopTipView *)popTipView
{
    [popTipView dismissAnimated:YES];
}

#pragma mark -
#pragma mark Public

- (void)promptInContainerView:(UIView *)container
{
    [self showPopUpWithMessage:_popUpMessage inView:container];
}

- (void)validate
{
    return;
}

- (void)finishEditing
{
    [self hidePopUp];
}

#pragma mark -
#pragma mark Change color (private)

- (void)changeBackgroundColor:(UIColor *)color animated:(BOOL)animated
{
    NSTimeInterval duration = 0;
    
    if (animated)
    {
        duration = 0.3;
    }

    [UIView animateWithDuration:duration
                          delay:0
                        options:UIViewAnimationOptionCurveEaseOut
                     animations:^
                        {
                            [self setBackgroundColor:color];
                        }
                     completion:nil];
}

#pragma mark -
#pragma mark Show/hide popup

- (void)showPopUpWithMessage:(NSString *)msg inView:(UIView *)view
{
    if ([_popUp superview] && [msg isEqualToString:[_popUp message]])
    {
        return;
    }
    
    [_popUp setMessage:msg];
    [_popUp presentPointingAtView:self inView:view animated:YES];
}

- (void)hidePopUp
{
    if ([_popUp superview])
    {
        [_popUp dismissAnimated:YES];
    }
}

#pragma mark -
#pragma mark KVO

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if ([keyPath isEqualToString:@"valid"])
    {
        if (_valid)
        {
            [self changeBackgroundColor:CG_GREEN_COLOR animated:YES];
            
            return;
        }

        [self changeBackgroundColor:RGB(139, 0, 0) animated:YES];
    }
}

@end

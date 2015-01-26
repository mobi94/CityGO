//
//  GFLastNameTextField.m
//  Gracefull
//
//  Created by curly0nion on 10/14/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

#import "GFLastNameTextField.h"
#import "GFRequiredSignUpTextFieldProtected.h"
#import "GFLimitedInputTextFieldProtected.h"

@implementation GFLastNameTextField

#pragma mark -
#pragma mark Lifecycle

- (void)awakeFromNib
{
    [super awakeFromNib];
    
    [self setMaxLength:60];
}

#pragma mark -
#pragma mark Public

- (void)promptInContainerView:(UIView *)container
{
    [self setPopUpMessage:@"1 to 60 characters"];
    
    [super promptInContainerView:container];
}

- (void)validate
{
//        [self showPopUpWithMessage:@"Please enter your last name" inView:container];

    [self setValid:[[self text] length] != 0];
}

@end

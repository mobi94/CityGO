//
//  GFEmailTextField.m
//  Gracefull
//
//  Created by curly0nion on 9/19/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

#import "GFEmailTextField.h"
#import "GFRequiredSignUpTextFieldProtected.h"
#import "GFLimitedInputTextFieldProtected.h"

@implementation GFEmailTextField

- (void)awakeFromNib
{
    [super awakeFromNib];
    
    [self setMaxLength:200];
}

- (void)promptInContainerView:(UIView *)container
{
    return;
}

- (void)validate
{
    NSString *emailRegex = @"[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
    NSPredicate *emailTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", emailRegex];
    
    [self setValid:[emailTest evaluateWithObject:[self text]]];
}

@end

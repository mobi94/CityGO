//
//  GFPasswordTextField.m
//  Gracefull
//
//  Created by curly0nion on 9/19/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

#import "GFPasswordTextField.h"
#import "GFRequiredSignUpTextFieldProtected.h"
#import "GFLimitedInputTextFieldProtected.h"

@implementation GFPasswordTextField

- (void)awakeFromNib
{
    [super awakeFromNib];
    
    [self setMaxLength:200];
}

#pragma mark -
#pragma mark Prompting/validation (Public)

- (void)promptInContainerView:(UIView *)container
{
    [self setPopUpMessage:@"A password must contain at least: 8 characters, 1 uppercase letter, 1 digit"];
    
    [super promptInContainerView:container];
}

- (void)validate
{
    if ([[self text] length] < 8)
    {
//        [self showPopUpWithMessage:@"Your password must be at least 8 characters long" inView:container];

        [self setValid:NO];
        
        return;
    }
    
    NSRange upperCaseRange = [[self text] rangeOfCharacterFromSet:[NSCharacterSet uppercaseLetterCharacterSet]];
    
    if (upperCaseRange.location == NSNotFound)
    {
//        [self showPopUpWithMessage:@"Your password must have at least 1 UPPERCASE letter" inView:container];
        
        [self setValid:NO];

        return;
    }
    
    NSRange decimalRange = [[self text] rangeOfCharacterFromSet:[NSCharacterSet decimalDigitCharacterSet]];
    
    if (decimalRange.location == NSNotFound)
    {
//        [self showPopUpWithMessage:@"Your password must contain at least 1 digit" inView:container];
        
        [self setValid:NO];

        return;
    }
    
    [self setValid:YES];
}

#pragma mark -
#pragma mark Text input

- (BOOL)shouldChangeTextInRange:(UITextRange *)range replacementText:(NSString *)text
{
    NSUInteger cursorOffset = [self offsetFromPosition:[self beginningOfDocument] toPosition:[range start]];
    
    BOOL whitespace = [[text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] isEqualToString:@""];
    BOOL blank = [text isEqualToString:@""];
    BOOL backspace = NO;
    
    if (blank)
    {
        backspace = (cursorOffset < [[self text] length]);
    }

    if (![super shouldChangeTextInRange:range replacementText:text] ||
        (whitespace && !backspace))
    {
        return NO;
    }
    
    return YES;
}

@end

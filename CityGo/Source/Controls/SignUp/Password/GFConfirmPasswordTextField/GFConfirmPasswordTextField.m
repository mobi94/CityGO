//
//  GFConfirmPasswordTextField.m
//  Gracefull
//
//  Created by curly0nion on 9/19/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

#import "GFConfirmPasswordTextField.h"
#import "GFRequiredSignUpTextFieldProtected.h"
#import "GFLimitedInputTextFieldProtected.h"
#import "GFPasswordTextField.h"

@interface GFConfirmPasswordTextField ()

@property(weak, nonatomic) IBOutlet GFPasswordTextField *passwordToMatch;

@end

@implementation GFConfirmPasswordTextField

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
    if (![_passwordToMatch isValid] || ![[_passwordToMatch text] isEqualToString:[self text]])
    {
//        [self showPopUpWithMessage:@"Password mismatch" inView:container];
        
        [self setValid:NO];
        
        return;
    }
    
    [self setValid:YES];
}

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
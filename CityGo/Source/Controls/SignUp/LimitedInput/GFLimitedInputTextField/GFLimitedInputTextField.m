//
//  GFLimitedInputTextField.m
//  Gracefull
//
//  Created by curly0nion on 10/17/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

#import "GFLimitedInputTextField.h"
#import "GFLimitedInputTextFieldProtected.h"

@implementation GFLimitedInputTextField

- (BOOL)shouldChangeTextInRange:(UITextRange *)range replacementText:(NSString *)text
{
    NSUInteger cursorOffset = [self offsetFromPosition:[self beginningOfDocument] toPosition:[range start]];
    
    NSAssert(_maxLength, @"Max length is not specified");
    
    NSLog(@"Start Offset: %lu, Full Length: %lu", (unsigned long)cursorOffset, (unsigned long)[[self text] length]);
    
    BOOL firstSymbol = cursorOffset == 0;
    BOOL whitespace = [[text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] isEqualToString:@""];
    BOOL blank = [text isEqualToString:@""];
    BOOL backspace = NO;
    
    if (blank)
    {
        backspace = (cursorOffset < [[self text] length]);
    }
    
    NSUInteger symbolsRemaining = [self maxLength] - [[self text] length];
    
    BOOL cursorReachedTheEnd = cursorOffset >= _maxLength;
    BOOL maxLengthWasExceeded = [text length] > symbolsRemaining;

    return (!(firstSymbol && whitespace) && !cursorReachedTheEnd && !maxLengthWasExceeded) || backspace;
}

@end
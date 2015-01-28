//
//  GFSignInTextField.m
//  Gracefull
//
//  Created by curly0nion on 10/12/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

#import "GFSignInTextField.h"

@implementation GFSignInTextField

- (BOOL)isEmpty
{
    return [[self text] length] == 0;
}

#pragma mark -
#pragma mark Custom text rect

- (CGRect)textRectForBounds:(CGRect)bounds
{
    return CGRectOffset(CGRectInset(bounds, 17, 10), -7, 0);
}

- (CGRect)editingRectForBounds:(CGRect)bounds
{
    return CGRectOffset(CGRectInset(bounds, 17, 10), -7, 0);
}

@end

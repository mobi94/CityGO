//
//  UITextField+NextTextField.m
//  Gracefull
//
//  Created by curly0nion on 9/18/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

@import ObjectiveC;

#import "UITextField+NextTextField.h"

static char defaultHashKey;

@implementation UITextField (NextTextField)

- (UITextField *)nextTextField
{
    return objc_getAssociatedObject(self, &defaultHashKey);
}

- (void)setNextTextField:(UITextField *)nextTextField
{
    objc_setAssociatedObject(self, &defaultHashKey, nextTextField, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

@end

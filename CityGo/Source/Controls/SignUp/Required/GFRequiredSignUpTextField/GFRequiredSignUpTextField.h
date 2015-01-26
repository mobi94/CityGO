//
//  GFRequiredSignUpTextField.h
//  Gracefull
//
//  Created by curly0nion on 9/19/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

#import "GFSignUpTextField.h"

@interface GFRequiredSignUpTextField : GFSignUpTextField

- (void)validate;
- (BOOL)isValid;

- (void)promptInContainerView:(UIView *)container;

- (void)finishEditing;

@end

//
//  GFRequiredSignUpTextFieldProtected.h
//  Gracefull
//
//  Created by curly0nion on 10/14/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

@interface GFRequiredSignUpTextField ()

@property(strong, nonatomic) NSString *popUpMessage;

@property(assign, nonatomic, getter = isValid) BOOL valid;

@end

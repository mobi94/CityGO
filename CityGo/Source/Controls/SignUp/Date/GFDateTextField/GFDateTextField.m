//
//  GFDateTextField.m
//  Gracefull
//
//  Created by curly0nion on 9/23/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

#import "GFDateTextField.h"
#import "GFRequiredSignUpTextFieldProtected.h"
#import "GFLimitedInputTextFieldProtected.h"

@implementation GFDateTextField

- (NSString *)text
{
    NSDateFormatter *dateFormatter = [NSDateFormatter new];
    [dateFormatter setDateStyle:NSDateFormatterLongStyle];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    
    NSString *dateString = [super text];
    NSDate *dateToFormat = [dateFormatter dateFromString:dateString];
    
    [dateFormatter setDateFormat:@"dd.MM.yyyy"];
    
    return [dateFormatter stringFromDate:dateToFormat];
}

- (void)promptInContainerView:(UIView *)container
{
    [self setPopUpMessage:@"This field help us"];
    
    [super promptInContainerView:container];
}

- (void)validate
{
    [self setValid:(self.text.length > 0)];
}

@end

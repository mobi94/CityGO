//
//  GFAuthTextField.m
//  Gracefull
//
//  Created by curly0nion on 10/6/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

#import "GFAuthTextField.h"

@interface GFAuthTextField ()

@property(strong, nonatomic, readwrite) NSString *identifier;

@end

@implementation GFAuthTextField

#pragma mark -
#pragma mark Lifecycle

- (void)awakeFromNib
{
    [super awakeFromNib];
    
    [self setBackgroundColor:[UIColor clearColor]];
    [self setTextColor:CG_WHITE_COLOR];
    [[self layer] setBorderWidth:0.5];
    [[self layer] setBorderColor:CG_WHITE_COLOR.CGColor];
    [[self layer] setCornerRadius:4];
    [self setClipsToBounds:YES];
    
    NSAttributedString *attrPlaceholder = [[NSAttributedString alloc] initWithString:[self placeholder]
                                                                          attributes:@{NSForegroundColorAttributeName : CG_WHITE_COLOR}];
    [self setAttributedPlaceholder:attrPlaceholder];
}

#pragma mark -
#pragma mark Public

- (void)setupIdentifier
{
    if ([self placeholder])
    {
        NSRange rangeOfParanthesisedString = [[self placeholder] rangeOfString:@"(" options:NSBackwardsSearch];
        
        NSString *noParenthesesPlaceholder = [self placeholder];
        
        if (rangeOfParanthesisedString.location != NSNotFound)
        {
            noParenthesesPlaceholder = [[self placeholder] substringToIndex:rangeOfParanthesisedString.location];
        }
        
        NSString *noSpacesPlaceholder = [noParenthesesPlaceholder stringByReplacingOccurrencesOfString:@" " withString:@""];
        
        NSString *lowerCasedPlaceholder = [noSpacesPlaceholder lowercaseString];
        
        NSString *identifier = [lowerCasedPlaceholder stringByReplacingOccurrencesOfString:@"-" withString:@""];
        
        [self setIdentifier:identifier];
        
        return;
    }
    
    [self setIdentifier:@"default"];
}

@end
